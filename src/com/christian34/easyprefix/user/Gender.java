package com.christian34.easyprefix.user;

import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.messages.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Gender {
    private static List<String> types;
    private static ConcurrentHashMap<String, Gender> genderTypes;
    private String type, name;

    private Gender(String id) {
        this.type = id.toLowerCase();
        this.name = Messages.getText("gender." + getId());
        if (this.name == null) {
            genderTypes.remove(getId());
            types.remove(getId());
        }
    }

    public static void load() {
        types = new ArrayList<>();
        List<String> t = FileManager.getConfig().getFileData().getStringList("config.gender.types");
        for (String target : t) {
            if (Messages.getText("gender." + target) != null) {
                types.add(target.toLowerCase());
            } else {
                Messages.log("&cCouldn't recognize gender '" + target + "'. " + "Please add a name to the language file. " + "If you're not sure what to do look on the wiki page of EasyPrefix");
            }
        }
        genderTypes = new ConcurrentHashMap<>();
        for (String target : types) {
            genderTypes.put(target, new Gender(target));
        }
    }

    public static Gender get(String name) {
        return getGenderTypes().getOrDefault(name, null);
    }

    public static ConcurrentHashMap<String, Gender> getGenderTypes() {
        return genderTypes;
    }

    public static List<String> getTypes() {
        return types;
    }

    public String getId() {
        return type;
    }

    public String getName() {
        return name;
    }

}