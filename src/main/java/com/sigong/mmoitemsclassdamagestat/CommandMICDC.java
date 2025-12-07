package com.sigong.mmoitemsclassdamagestat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandMICDC implements CommandExecutor {
    public CommandMICDC(MMOItemsClassDamageStat plugin) {
        this.plugin = plugin;
    }

    private MMOItemsClassDamageStat plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("reload")){
                sender.sendMessage(ChatColor.GREEN + "[MMOItemsClassDamageStat] Reloading...");
                plugin.reload();
                return true;
            }else if(args[0].equalsIgnoreCase("toggledebug")){
                sender.sendMessage(ChatColor.GREEN + "[MMOItemsClassDamageStat] Toggled console debug messages.");
                plugin.toggleDebug();
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Improper usage: proper usage is /micdc [reload | toggledebug]");
        return true;
    }
}
