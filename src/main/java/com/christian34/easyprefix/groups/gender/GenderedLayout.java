package com.christian34.easyprefix.groups.gender;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.sql.database.SQLDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public GenderedLayout(EasyGroup easyGroup) {
        int type = (easyGroup instanceof Group) ? 0 : 1;
        EasyPrefix instance = EasyPrefix.getInstance();
        GroupHandler groupHandler = instance.getGroupHandler();
        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        if (instance.getSqlDatabase() != null) {
            SQLDatabase database = instance.getSqlDatabase();
            try {
                String sql = "SELECT `gender`, `prefix`, `suffix` FROM `%p%genders` WHERE `type` = " + type + " AND " + "`group_name` = '" + easyGroup.getName() + "'";
                ResultSet result = database.getValue(sql);
                if (result == null) return;
                while (result.next()) {
                    String genderName = result.getString("gender");
                    Gender gender = groupHandler.getGender(genderName);
                    if (gender != null) {
                        String prefix = result.getString("prefix");
                        if (prefix != null) prefixes.put(gender, prefix);
                        String suffix = result.getString("suffix");
                        if (suffix != null) suffixes.put(gender, suffix);
                    } else {
                        Messages.log("error GC_01");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            GroupsData groupsData = instance.getFileManager().getGroupsData();
            Set<String> set = groupsData.getSection(easyGroup.getFilePath() + "genders");
            if (!set.isEmpty()) {
                for (String genderName : set) {
                    Gender gender = groupHandler.getGender(genderName);
                    if (gender == null) {
                        Messages.log("error GC_02: " + genderName);
                        continue;
                    }
                    String prefix = groupsData.getData().getString(easyGroup.getFilePath() + "genders." + genderName + ".prefix");
                    if (prefix != null) prefixes.put(gender, prefix);
                    String suffix = groupsData.getData().getString(easyGroup.getFilePath() + "genders." + genderName + ".suffix");
                    if (suffix != null) suffixes.put(gender, suffix);
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
