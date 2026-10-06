# Development

## Requirements

- **JDK 25** to build (the plugin itself targets Java 21, so it runs on 1.21.x servers)
- Gradle comes with the wrapper (`./gradlew`)

## Building

```bash
./gradlew build
```

The shaded jar lands in `release/`. `build` also runs the tests - skip them with `-x test`.

Libraries (Cloud, InventoryGui, bStats, ConfigUpdater) are shaded and relocated. Adventure, MiniMessage and the
MySQL driver come with Paper and are not shaded.

### Local settings

Put these into `gradle.properties` (it is gitignored):

| Property | Description |
|---|---|
| `serverPluginsDir=<path>/plugins` | `build` copies the jar to `<plugins>/update/`, so the server loads it on the next start |
| `testMysql=host:port/database;user;password` | also run the MySQL tests (tables `eptest_*`) - the build fails if the database is not reachable |
| `relocateLibraries=false` | skip relocating for HotSwap development - **never release such a jar** |

With `serverPluginsDir` set, `./gradlew testServer -Pcommands="ep help;color show"` starts the local test server, runs
the `;`-separated console commands, stops it and prints a summary of warnings and errors
([scripts/test-server.ps1](../scripts/test-server.ps1)).

## Tests

```bash
./gradlew test
```

Tests use JUnit and [MockBukkit](https://github.com/MockBukkit/MockBukkit). Extend `PluginTestBase` - every test gets a
fresh mocked server with the plugin enabled:

```java
class MyTest extends PluginTestBase {
    @Test
    void setsColor() {
        PlayerMock player = addPlayer("Steve", "EasyPrefix.color.red");
        execute(player, "color set red");
        assertContains(messages(player), "Red");
    }
}
```

- `addPlayer(name, permissions...)` - permissions must be given here, the user is loaded on join
- `execute(sender, command)` runs a command synchronously and returns the failure (or `null`)
- `messages(sender)` returns the received messages without colors

Things MockBukkit can't do: dialogs (use `UserInput.setFactory`), PlaceholderAPI (call `CustomPlaceholder#onRequest`
directly), the Paper command manager (tests use `TestCommandManager`).

## Code layout

`src/main/java/com/christian34/easyprefix`:

| Package | Content |
|---|---|
| `EasyPrefix` | main class, colors, formattings, users, reload |
| `commands` | Cloud commands (`/ep`, `/color`, `/tags`), argument parsers in `arguments` |
| `listeners` | chat formatting (`ChatListener`), tab list and name tags (`DisplayManager`), hover, mentions, join/quit |
| `groups` | groups and tags (subgroups), `GroupHandler` |
| `user` | `User`, its data and custom prefixes |
| `files` | `config.yml`, `messages.yml`, `groups.yml` |
| `sql` | storage: SQLite and MySQL with HikariCP, schema migrations, multi-server sync |
| `extensions` | PlaceholderAPI expansion and Vault chat provider |
| `utils` | GUIs (`UserInterface`, `GuiCreator`), colors, text input, tasks, updater |

### Conventions

- **Folia**: never use the `BukkitScheduler`, only `TaskManager` (`global`, `run(player, ...)`, `async`).
- **New messages**: add the key to `messages.yml` **and** the `Message` enum.
- **Schema changes**: add a new `Migration` at the end of `SchemaMigrations.MIGRATIONS` (for MySQL and SQLite),
  never change a released one.
- **Database connections**: always try-with-resources or `Database#query`, they come from a pool.
- **Texts for other plugins** (PlaceholderAPI, Vault) are legacy colors with all placeholders resolved
  (`ExpansionManager#toLegacy`).

## Releasing

1. Bump `version` in `build.gradle.kts` and commit it.
2. Tag it with the release note: `git tag -a v<version> -F notes.md` and `git push origin v<version>`.

The [release workflow](../.github/workflows/release.yml) builds the jar with tests, creates the GitHub release and
publishes it to Modrinth and Hangar. Versions with a `-` (e.g. `2.1.0-beta.1`) are pre-releases and only go to
GitHub. Supported Minecraft versions are set in `minecraftVersions` in `build.gradle.kts`. SpigotMC is updated by
hand.
