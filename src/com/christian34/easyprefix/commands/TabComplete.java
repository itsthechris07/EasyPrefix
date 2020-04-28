package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.user.Gender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabComplete implements TabCompleter {
    private EasyPrefix instance;

    public TabComplete(EasyPrefix instance) {
        this.instance = instance;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("easyprefix")) {
            ArrayList<String> cmds = new ArrayList<>();
            if (args.length == 1) {
                if (sender.hasPermission("EasyPrefix.admin")) {
                    cmds.add("reload");
                    cmds.add("set");
                    cmds.add("setup");
                    cmds.add("user");
                    cmds.add("database");
                }
                if (sender.hasPermission("EasyPrefix.settings")) {
                    cmds.add("settings");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("user")) {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        cmds.add(target.getDisplayName());
                    }
                } else if (args[0].equalsIgnoreCase("database")) {
                    cmds.add("upload");
                    cmds.add("download");
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("user")) {
                    cmds.add("info");
                    cmds.add("update");
                    cmds.add("setgroup");
                    cmds.add("setsubgroup");
                    cmds.add("setgender");
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("user")) {
                    if (args[2].equalsIgnoreCase("setgroup")) {
                        for (Group targetGroup : this.instance.getGroupHandler().getGroups()) {
                            cmds.add(targetGroup.getName());
                        }
                    } else if (args[2].equalsIgnoreCase("setsubgroup")) {
                        for (Subgroup targetGroup : this.instance.getGroupHandler().getSubgroups()) {
                            cmds.add(targetGroup.getName());
                        }
                    } else if (args[2].equalsIgnoreCase("setgender")) {
                        cmds.addAll(Gender.getTypes());
                    }
                }
            }
            Collections.sort(cmds);
            return cmds;
        }
        return null;
    }

}