package com.sigong.mmoitemsclassdamagestat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MMOItemsClassDamageStat extends JavaPlugin {

    private PlayerAttackListener attackListener;

    @Override
    public void onEnable(){
        this.getCommand("micdc").setExecutor(new CommandMICDC(this));

        //saves default config if it doesn't already exist in plugin folder
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MMOCoreEnableListener(this),this);
        Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] Registered MMOCore enable listener.");

        attackListener = new PlayerAttackListener(this);
        getServer().getPluginManager().registerEvents(attackListener, this);
        Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] Registered PlayerAttackEvent listener.");

        Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] MMOItemsClassDamageStat has been enabled.");
    }

    //Reloads the config, then instructs the attackListener to retrieve the values it uses from the config.
    public void reload(){
        reloadConfig();
        attackListener.reload();
    };

    //Toggles the displaying of debug messages in the attackListener
    public void toggleDebug(){
        attackListener.toggleDebug();
    };

    @Override
    public void onDisable(){
        Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] MMOItemsClassDamageStat has been disabled.");
    }
}
