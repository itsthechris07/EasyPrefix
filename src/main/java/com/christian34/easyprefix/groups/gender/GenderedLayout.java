package com.christian34.easyprefix.groups.gender;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class GenderedLayout {
    private final Map<Gender, String> prefixes;
    private final Map<Gender, String> suffixes;
    private final EasyGroup easyGroup;
    private final EasyPrefix instance;
    private final String easyGroupType;

    public GenderedLayout(EasyGroup easyGroup) {
        this.easyGroup = easyGroup;
        this.instance = EasyPrefix.getInstance();
        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        this.easyGroupType = easyGroup instanceof Group ? "group" : "subgroup";
        load();
    }

    public Map<Gender, String> getPrefixes() {
        return prefixes;
    }

    public Map<Gender, String> getSuffixes() {
        return suffixes;
    }

    private void load() {
        GroupHandler groupHandler = instance.getGroupHandler();
        if (instance.getStorageType() == StorageType.SQL) {
            SQLDatabase database = instance.getSqlDatabase();
            String sql = "SELECT `gender`, `prefix`, `suffix` FROM `%p%" + easyGroupType + "s_gendered` " + "WHERE `group` = '" + easyGroup.getName() + "'";
            try (ResultSet result = database.getValue(sql)) {
                while (result != null && result.next()) {
                    Gender gender = groupHandler.getGender(result.getString("gender"));

                    if (gender == null) {
                        Debug.log("§cYou've used an invalid gender for " + easyGroupType + " '" + easyGroup.getName() + "'!");
                        continue;
                    }

                    String prefix = result.getString("prefix");
                    if (prefix != null) {
                        prefixes.put(gender, prefix);
                    }

                    String suffix = result.getString("suffix");
                    if (suffix != null) {
                        suffixes.put(gender, suffix);
                    }
                }
            } catch (SQLException ex) {
                Debug.catchException(ex);
            }
        } else {
            GroupsData groupsData = instance.getFileManager().getGroupsData();
            assert groupsData != null;
            ConfigurationSection section = groupsData.getSection(easyGroup.getFileKey() + "genders");
            if (section == null) {
                return;
            }
            for (String name : section.getKeys(false)) {
                Gender gender = groupHandler.getGender(name);

                if (gender == null) {
                    Debug.log("§cYou've used an invalid gender for " + easyGroupType + " '" + easyGroup.getName() + "'!");
                    continue;
                }

                String prefix = groupsData.getData().getString(easyGroup.getFileKey() + "genders." + name + ".prefix");
                if (prefix != null) {
                    prefixes.put(gender, prefix);
                }

                String suffix = groupsData.getData().getString(easyGroup.getFileKey() + "genders." + name + ".suffix");
                if (suffix != null) {
                    suffixes.put(gender, suffix);
                }
            }
        }
    }

    @Nullable
    public String getPrefix(Gender gender) {
        return this.prefixes.get(gender);
    }

    @Nullable
    public String getSuffix(Gender gender) {
        return this.suffixes.get(gender);
    }

}
