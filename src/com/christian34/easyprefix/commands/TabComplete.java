package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.GenderType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class TabComplete implements TabCompleter {
    private EasyPrefix instance;

    public TabComplete(EasyPrefix instance) {
        this.instance = instance;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("easyprefix")) return null;
        ArrayList<String> matches = new ArrayList<>();
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("reload", "set", "setup", "user", "group"));
            if (this.instance.getSqlDatabase() != null) list.add("database");
            if (sender.hasPermission("EasyPrefix.settings")) {
                list.add("settings");
            }
            if (!args[0].isEmpty()) {
                for (String match : list) {
                    if (match.toLowerCase().startsWith(args[0].toLowerCase())) matches.add(match);
                }
            } else {
                if (sender.hasPermission("EasyPrefix.admin")) {
                    matches.addAll(list);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("user")) {
                return null;
            } else if (args[0].equalsIgnoreCase("database")) {
                if (this.instance.getSqlDatabase() == null) return matches;
                List<String> list = Arrays.asList("upload", "download");
                if (!args[1].isEmpty()) {
                    for (String match : list) {
                        if (match.toLowerCase().startsWith(args[1].toLowerCase()))
                            return Collections.singletonList(match);
                    }
                } else {
                    matches.addAll(list);
                }
            } else if (args[0].equalsIgnoreCase("group")) {
                List<String> list = new ArrayList<>();
                for (Group group : EasyPrefix.getInstance().getGroupHandler().getGroups()) {
                    list.add(group.getName());
                }
                if (!args[1].isEmpty()) {
                    for (String match : list) {
                        if (match.toLowerCase().startsWith(args[1].toLowerCase()))
                            return Collections.singletonList(match);
                    }
                } else {
                    matches.addAll(list);
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("user")) {
                List<String> list = Arrays.asList("info", "reload", "setgroup", "setsubgroup", "setgender");
                if (!args[2].isEmpty()) {
                    for (String match : list) {
                        if (match.toLowerCase().startsWith(args[2].toLowerCase())) matches.add(match);
                    }
                } else {
                    matches.addAll(list);
                }
            } else if (args[0].equalsIgnoreCase("group")) {
                matches.add("info");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("setgroup")) {
                    ArrayList<Group> groups = this.instance.getGroupHandler().getGroups();
                    for (Group targetGroup : groups) {
                        if (!args[3].isEmpty()) {
                            if (targetGroup.getName().toLowerCase().startsWith(args[3].toLowerCase()))
                                matches.add(targetGroup.getName());
                        } else {
                            matches.add(targetGroup.getName());
                        }
                    }
                } else if (args[2].equalsIgnoreCase("setsubgroup")) {
                    ArrayList<Subgroup> groups = this.instance.getGroupHandler().getSubgroups();
                    for (Subgroup targetGroup : groups) {
                        if (!args[3].isEmpty()) {
                            if (targetGroup.getName().toLowerCase().startsWith(args[3].toLowerCase()))
                                matches.add(targetGroup.getName());
                        } else {
                            matches.add(targetGroup.getName());
                        }
                    }
                    matches.add("none");
                } else if (args[2].equalsIgnoreCase("setgender")) {
                    for (GenderType genderType : EasyPrefix.getInstance().getGroupHandler().getGenderTypes()) {
                        String targetGroup = genderType.getName();
                        if (!args[3].isEmpty()) {
                            if (targetGroup.toLowerCase().startsWith(args[3].toLowerCase())) matches.add(targetGroup);
                        } else {
                            matches.add(targetGroup);
                        }
                    }
                }
            }
        }
        Collections.sort(matches);
        return matches;
    }

}