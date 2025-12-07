package com.sigong.mmoitemsclassdamagestat;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MMOCoreEnableListener implements Listener {

    MMOCoreEnableListener(MMOItemsClassDamageStat plugin){
        this.plugin = plugin;
    }

    private final MMOItemsClassDamageStat plugin;

    /* This is implemented using an EventHandler because I want to procedurally generate the stats based off the class list from MMOCore
    * But you can't get the class list until MMOCore enables, so I listen for the enable after my plugin loads and
    * register the stats before MMOItems enables (since this loads before both, this listener will be registered in time)*/
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event){
        //If MMOCore has just been enabled and MMOItems is loaded
        if(event.getPlugin().getName().equals("MMOCore") && Bukkit.getPluginManager().getPlugin("MMOItems") != null){

            Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] MMOCore has been enabled, MMOItems is loaded, stats will now be registered.");

            //copied from the code that displays the class choosing screen in MMOCore
            /*List<PlayerClass> classes = (List<PlayerClass>) MMOCore.plugin.classManager.getAll().stream().filter((var0) -> {
                return var0.hasOption(ClassOption.DISPLAY);
            }).sorted(Comparator.comparingInt(PlayerClass::getDisplayOrder)).collect(Collectors.toList());*/

            List<PlayerClass> classes = (List<PlayerClass>) MMOCore.plugin.classManager.getAll().stream().sorted(Comparator.comparingInt(PlayerClass::getDisplayOrder)).collect(Collectors.toList());

            //Create the two ArrayLists that contain the lines to paste
            ArrayList<String> statLines = new ArrayList<>();
            ArrayList<String> loreFormatLines = new ArrayList<>();

            //For each class that has been registered in MMOCore, register 4 stats in MMOItems and add lines to ArrayLists to paste manually
            for(PlayerClass pc : classes){

                Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] Registering stats for " + pc.getName() + ".");

                //RAW Damage Bonus TO Class
                MMOItems.plugin.getStats().register(new DoubleStat( "RAW_DAMAGE_TO_" + pc.getId(),
                                                                        Material.DIAMOND_BLOCK,
                                                                    "Raw Damage To " + pc.getName(),
                                                                        new String[]{"Raw modification to damage dealt", "to " + pc.getName() + " by this item."},
                                                                        new String[]{"all"},
                                                                     true)); //moreisbetter
                Bukkit.getConsoleSender().sendMessage("    Registered stat: RAW_DAMAGE_TO_" + pc.getId());

                //PERCENTAGE Damage Bonus TO Class
                MMOItems.plugin.getStats().register(new DoubleStat( "PERCENT_DAMAGE_TO_" + pc.getId(),
                                                                        Material.DIAMOND_BLOCK,
                                                                    "Percent Damage To " + pc.getName(),
                                                                        new String[]{"Percent modification to damage dealt", "to " + pc.getName() + " by this item."},
                                                                        new String[]{"all"},
                                                                     true)); //moreisbetter
                Bukkit.getConsoleSender().sendMessage("    Registered stat: PERCENT_DAMAGE_TO_" + pc.getId());

                //Raw Damage Reduction FROM class
                MMOItems.plugin.getStats().register(new DoubleStat( "RAW_DAMAGE_FROM_" + pc.getId(),
                                                                        Material.EMERALD_BLOCK,
                                                                    "Raw Damage From " + pc.getName(),
                                                                        new String[]{"Raw modification to damage", "received from " + pc.getName() + "."},
                                                                        new String[]{"all"},
                                                                    false)); //moreisbetter
                Bukkit.getConsoleSender().sendMessage("    Registered stat: RAW_DAMAGE_FROM_" + pc.getId());

                //PERCENTAGE Damage Reduction FROM Class
                MMOItems.plugin.getStats().register(new DoubleStat( "PERCENT_DAMAGE_FROM_" + pc.getId(),
                                                                        Material.EMERALD_BLOCK,
                                                                    "Percent Damage From " + pc.getName(),
                                                                        new String[]{"Percent modification to damage", "received from " + pc.getName() + "."},
                                                                        new String[]{"all"},
                                                                    false)); //moreisbetter
                Bukkit.getConsoleSender().sendMessage("    Registered stat: PERCENT_DAMAGE_FROM_" + pc.getId());

                //Add the appropriate lines to the text files to paste
                statLines.add("raw-damage-to-" + pc.getId().toLowerCase() + ": '&3 &7➸ Damage to " + pc.getName() + ": &f<plus>#'");
                statLines.add("percent-damage-to-" + pc.getId().toLowerCase() + ": '&3 &7➸ Damage to " + pc.getName() + ": &f<plus>#%'");
                statLines.add("raw-damage-from-" + pc.getId().toLowerCase() + ": '&3 &7■ Damage from " + pc.getName() + ": &f<plus>#'");
                statLines.add("percent-damage-from-" + pc.getId().toLowerCase() + ": '&3 &7■ Damage from " + pc.getName() + ": &f<plus>#%'");
                loreFormatLines.add("- '#raw-damage-to-" + pc.getId().toLowerCase() + "#'");
                loreFormatLines.add("- '#percent-damage-to-" + pc.getId().toLowerCase() + "#'");
                loreFormatLines.add("- '#raw-damage-from-" + pc.getId().toLowerCase() + "#'");
                loreFormatLines.add("- '#percent-damage-from-" + pc.getId().toLowerCase() + "#'");
            }

            Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] All stats have been registered.");

            //Create file writers and write the lines that will need to be pasted to the two config files to them
            try {
                File file = plugin.getDataFolder();
                file.mkdirs();

                BufferedWriter statsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + File.separator + "paste to stats.yml .txt"), StandardCharsets.UTF_8));
                BufferedWriter loreFormatWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + File.separator + "paste to lore-format.yml .txt"), StandardCharsets.UTF_8));

                for(String s : statLines){
                    statsWriter.write(s + "\n");
                }
                for(String s : loreFormatLines){
                    loreFormatWriter.write(s + "\n");
                }

                statsWriter.close();
                loreFormatWriter.close();

            } catch (IOException e) {
                System.out.println(ChatColor.RED + "[MMOItemsClassDamageStat] An error occurred when creating, writing to, " +
                                    "or closing the FileWriters for the text files containing the lines to paste into stats.yml and lore-format.yml. " +
                                    "The stats can still be applied to items and will still have an effect, but until these files are generated and you paste the lines into the appropriate yml files, " +
                                    "they will not be displayed in item lore.");
                e.printStackTrace();
            }
        }
    }
}
