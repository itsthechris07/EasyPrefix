package com.christian34.easyprefix.groups.gender;

import com.christian34.easyprefix.Database;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Messages;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GenderChat {
    private EasyGroup easyGroup;
    private EasyPrefix instance;
    private GroupHandler groupHandler;
    private HashMap<GenderType, String> prefixes;
    private HashMap<GenderType, String> suffixes;

    public GenderChat(EasyGroup easyGroup) {
        this.easyGroup = easyGroup;
        int type = (easyGroup instanceof Group) ? 0 : 1;
        this.instance = EasyPrefix.getInstance();
        this.groupHandler = instance.getGroupHandler();
        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        if (instance.getDatabase() != null) {
            Database database = instance.getDatabase();
            try {
                String sql = "SELECT `gender`, `prefix`, `suffix` FROM `%p%genders` WHERE `type` = " + type + " AND " + "`group_name` = '" + easyGroup.getName() + "'";
                ResultSet result = database.getValue(sql);
                while (result.next()) {
                    String genderName = result.getString("gender");
                    GenderType genderType = this.groupHandler.getGender(genderName);
                    if (genderType != null) {
                        String prefix = result.getString("prefix");
                        if (prefix != null) prefixes.put(genderType, prefix);
                        String suffix = result.getString("suffix");
                        if (suffix != null) suffixes.put(genderType, suffix);
                    } else {
                        Messages.log("error GC_01");
                    }
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
        } else {
            GroupsData groupsData = FileManager.getGroupsData();
            Set<String> set = groupsData.getSection(easyGroup.getFilePath() + "genders");
            if (!set.isEmpty()) {
                for (String genderName : set) {
                    GenderType genderType = this.groupHandler.getGender(genderName);
                    if (genderType == null) {
                        Messages.log("error GC_02: " + genderName);
                        continue;
                    }
                    String prefix = groupsData.getData().getString(easyGroup.getFilePath() + "genders." + genderName + ".prefix");
                    if (prefix != null) prefixes.put(genderType, prefix);
                    String suffix = groupsData.getData().getString(easyGroup.getFilePath() + "genders." + genderName + ".suffix");
                    if (suffix != null) suffixes.put(genderType, suffix);
                }
            }
        }
    }

    public String getPrefix(GenderType genderType) {
        return this.prefixes.getOrDefault(genderType, null);
    }

    public String getSuffix(GenderType genderType) {
        return this.suffixes.getOrDefault(genderType, null);
    }

}