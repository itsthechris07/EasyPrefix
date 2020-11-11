package com.christian34.easyprefix.groups.gender;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Set;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GenderedLayout {
    private final HashMap<Gender, String> prefixes;
    private final HashMap<Gender, String> suffixes;
    private final EasyGroup easyGroup;
    private final EasyPrefix instance;
    private final String easyGroupType;

    public GenderedLayout(EasyGroup easyGroup) {
        this.easyGroup = easyGroup;
        this.instance = EasyPrefix.getInstance();
        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        this.easyGroupType = easyGroup instanceof Group ? "group" : "subgroup";

        try {
            load();
        } catch (Exception ex) {
            Debug.log("§cAn error occurred while loading a gendered layout for "
                    + easyGroupType + " '" + easyGroup.getName() + "'!");
            ex.printStackTrace();
        }
    }

    private void load() throws Exception {
        if (instance.getStorageType() == StorageType.SQL) {
            SQLDatabase database = instance.getSqlDatabase();
            String sql = "SELECT `gender`, `prefix`, `suffix` FROM `%p%" + easyGroupType + "s_gendered` " +
                    "WHERE `group` = '" + easyGroup.getName() + "'";
            ResultSet result = database.getValue(sql);
            if (result == null) return;
            while (result.next()) {
                Gender gender = instance.getGroupHandler().getGender(result.getString("gender"));

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
        } else {
            GroupsData groupsData = instance.getFileManager().getGroupsData();
            Set<String> set = groupsData.getSection(easyGroup.getFileKey() + "genders");
            if (set.isEmpty()) {
                return;
            }
            for (String name : set) {
                Gender gender = instance.getGroupHandler().getGender(name);

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

    public String getPrefix(Gender gender) {
        return this.prefixes.get(gender);
    }

    public String getSuffix(Gender gender) {
        return this.suffixes.get(gender);
    }

}
