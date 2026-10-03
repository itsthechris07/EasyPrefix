import org.objectweb.asm.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath("org.ow2.asm:asm:9.10.1") }
}

plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.christian34.easyprefix"
version = "2.0.2"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.minebench.de/")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
}

val bukkitEnumsRewritten: Attribute<Boolean> = Attribute.of("bukkitEnumsRewritten", Boolean::class.javaObjectType)

dependencies {
    // Paper API already ships Adventure (incl. MiniMessage + legacy serializer)
    compileOnly("io.papermc.paper:paper-api:26.2.build.129-stable")
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("org.jetbrains:annotations:26.1.0")
    // VaultUnlocked (the Vault fork) contains the legacy Vault api (net.milkbowl.vault) and its new one (net.milkbowl.vault2)
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.20") {
        exclude(group = "org.bukkit")
    }
    compileOnly("org.apache.commons:commons-lang3:3.20.0")

    implementation("org.incendo:cloud-annotations:2.1.0")
    implementation("org.incendo:cloud-paper:2.0.1")
    implementation("org.incendo:cloud-minecraft-extras:2.0.1")

    implementation("de.themoep:inventorygui:1.6.5-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation("com.tchristofferson:ConfigUpdater:2.2")
    // connection pool for mysql and sqlite, slf4j is provided by paper
    implementation("com.zaxxer:HikariCP:7.1.0") {
        exclude(group = "org.slf4j")
    }

    // tests run the plugin against a mocked Paper server
    testImplementation("io.papermc.paper:paper-api:26.2.build.129-stable")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.2:4.116.1")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains:annotations:26.1.0")
    testImplementation("me.clip:placeholderapi:2.12.3")
    testImplementation("net.milkbowl.vault:VaultUnlockedAPI:2.20") {
        exclude(group = "org.bukkit")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // provided by the real server
    testRuntimeOnly("org.xerial:sqlite-jdbc:3.53.4.0")
    testRuntimeOnly("com.mysql:mysql-connector-j:9.2.0")
    testRuntimeOnly("org.apache.commons:commons-lang3:3.20.0")

    // see BukkitEnumRewrite
    attributesSchema { attribute(bukkitEnumsRewritten) }
    artifactTypes.getByName("jar") { attributes.attribute(bukkitEnumsRewritten, false) }
    registerTransform(BukkitEnumRewrite::class) {
        from.attribute(bukkitEnumsRewritten, false).attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jar")
        to.attribute(bukkitEnumsRewritten, true).attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "jar")
    }
}

configurations.testRuntimeClasspath {
    attributes.attribute(bukkitEnumsRewritten, true)
}

tasks {
    processResources {
        // read at configuration time, Task.project is not allowed while the task runs (Gradle 10)
        val pluginVersion = project.version.toString()
        inputs.property("version", pluginVersion)
        filesMatching("plugin.yml") {
            expand("version" to pluginVersion)
        }
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:removal"))
    }

    build {
        dependsOn(shadowJar)
    }
    // the plugin is only usable with its libraries, the plain jar would just be mistaken for it
    jar {
        enabled = false
    }

    test {
        useJUnitPlatform()
        // no bStats or update check from tests
        systemProperty("easyprefix.offline", "true")
        // optional mysql tests: -PtestMysql="host:port/database;user;password" (the tables get the prefix eptest_)
        providers.gradleProperty("testMysql").orNull?.let { systemProperty("easyprefix.test.mysql", it) }
        testLogging {
            events("failed", "skipped")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    // copies the plugin into the update folder of a local test server (swapped in on its next start);
    // set serverPluginsDir in the (git-ignored) gradle.properties to enable
    providers.gradleProperty("serverPluginsDir").orNull?.let { pluginsDir ->
        val deployPlugin = register<Copy>("deployPlugin") {
            from(shadowJar)
            into(file(pluginsDir).resolve("update"))
        }
        build {
            finalizedBy(deployPlugin)
        }

        // builds, deploys, starts the test server until it is ready and stops it again;
        // console commands to run in between: -Pcommands="ep debug;plugins"
        register<Exec>("testServer") {
            group = "verification"
            dependsOn(deployPlugin)
            val java = project.extensions.getByType<JavaToolchainService>()
                .launcherFor(java.toolchain).get().executablePath.asFile.absolutePath
            val commands = providers.gradleProperty("commands").orNull
            commandLine(buildList {
                addAll(
                    listOf(
                        "powershell",
                        "-NoProfile",
                        "-ExecutionPolicy",
                        "Bypass",
                        "-File",
                        file("scripts/test-server.ps1").absolutePath
                    )
                )
                addAll(listOf("-ServerDir", file(pluginsDir).parentFile.absolutePath, "-Java", java))
                if (commands != null) addAll(listOf("-Commands", commands))
            })
        }
    }
    shadowJar {
        archiveClassifier = ""
        // relocates the ServiceLoader files of cloud together with the classes; duplicates have to reach the
        // transformer, otherwise the entries of all but one library would be dropped
        filesMatching("META-INF/services/**") { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
        mergeServiceFiles()
        // the annotation processor of cloud-annotations is only needed at compile time
        exclude("META-INF/services/javax.annotation.processing.Processor", "META-INF/gradle/**", "META-INF/maven/**")
        // bStats refuses to start unless relocated
        relocate("org.bstats", "com.christian34.easyprefix.libs.bstats")
        // HotSwap replaces classes with unrelocated IDE-compiled ones, so local dev builds may skip relocation
        if (providers.gradleProperty("relocateLibraries").orNull == "false") {
            doFirst { logger.warn("relocateLibraries=false: libraries are not relocated, do not release this jar") }
            return@shadowJar
        }
        // release jars are kept outside of build/, so a clean or dev build does not remove them
        destinationDirectory = layout.projectDirectory.dir("release")
        relocate("org.incendo.cloud", "com.christian34.easyprefix.libs.cloud")
        relocate("io.leangen.geantyref", "com.christian34.easyprefix.libs.geantyref")
        relocate("de.themoep", "com.christian34.easyprefix.libs.inventorygui")
        relocate("com.tchristofferson", "com.christian34.easyprefix.libs.configupdater")
        relocate("com.zaxxer.hikari", "com.christian34.easyprefix.libs.hikari")
    }
}

/**
 * Bukkit enums like Sound became interfaces. Paper rewrites old plugin bytecode on load (enum method calls become
 * interface calls), MockBukkit doesn't - so InventoryGui, which is compiled against the old api, crashes in the tests.
 * This does the same rewrite for the test classpath only; the plugin jar is not touched.
 */
abstract class BukkitEnumRewrite : TransformAction<TransformParameters.None> {
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.get().asFile
        if (!input.name.startsWith("inventorygui")) {
            outputs.file(input)
            return
        }
        val output = outputs.file("rewritten-${input.name}")
        ZipFile(input).use { zip ->
            ZipOutputStream(output.outputStream()).use { out ->
                for (entry in zip.entries()) {
                    var bytes = zip.getInputStream(entry).use { it.readBytes() }
                    if (entry.name.endsWith(".class")) bytes = rewrite(bytes)
                    out.putNextEntry(ZipEntry(entry.name))
                    out.write(bytes)
                    out.closeEntry()
                }
            }
        }
    }

    private fun rewrite(bytes: ByteArray): ByteArray {
        val interfaces = setOf("org/bukkit/Sound")
        val reader = ClassReader(bytes)
        val writer = ClassWriter(reader, 0)
        reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {
            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val parent = super.visitMethod(access, name, descriptor, signature, exceptions)
                return object : MethodVisitor(Opcodes.ASM9, parent) {
                    override fun visitMethodInsn(
                        opcode: Int,
                        owner: String,
                        name: String,
                        descriptor: String,
                        isInterface: Boolean
                    ) {
                        if (owner in interfaces) {
                            val newOpcode = if (opcode == Opcodes.INVOKEVIRTUAL) Opcodes.INVOKEINTERFACE else opcode
                            super.visitMethodInsn(newOpcode, owner, name, descriptor, true)
                        } else {
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                        }
                    }
                }
            }
        }, 0)
        return writer.toByteArray()
    }
}
