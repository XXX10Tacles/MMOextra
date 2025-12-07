package com.sigong.mmoitemsclassdamagestat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.stat.StatInstance;
//import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.api.player.PlayerStats;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerAttackListener implements Listener {

    PlayerAttackListener(MMOItemsClassDamageStat plugin){
        this.plugin = plugin;

        getLimitsFromConfig();
    }

    //TODO: add descriptions for all methods

    // Retrieves (potentially) new values from the reloaded config
    public void reload(){
        Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] Reloading limits from config...");
        getLimitsFromConfig();
    };

    private boolean showDebugMessages = false;

    public void toggleDebug(){
        showDebugMessages = !showDebugMessages;
        Bukkit.getConsoleSender().sendMessage("[MMOItemsClassDamageStat] Debug messages set to " + showDebugMessages);
    }

    // Prints a given string to the console if debug messages are enabled 
    private void debugPrint(String message){
        debugPrint(message, Bukkit.getConsoleSender());
    }

    // Prints a given string to a CommandSender if debug messages are enabled
    private void debugPrint(String message, CommandSender recipient){
        if(showDebugMessages) {
            recipient.sendMessage(message);
        }
    };

    //Get effective stat Limits from config and print to console
    private void getLimitsFromConfig(){
        attacker_percent_upper_limit = plugin.getConfig().getDouble("limits.attacker.percent.upper-limit", Double.MAX_VALUE);
        attacker_percent_lower_limit = plugin.getConfig().getDouble("limits.attacker.percent.lower-limit", (-1D)*Double.MAX_VALUE);
        attacker_raw_upper_limit = plugin.getConfig().getDouble("limits.attacker.raw.upper-limit", Double.MAX_VALUE);
        attacker_raw_lower_limit = plugin.getConfig().getDouble("limits.attacker.raw.lower-limit", (-1D)*Double.MAX_VALUE);
        defender_percent_upper_limit = plugin.getConfig().getDouble("limits.defender.percent.upper-limit", Double.MAX_VALUE);
        defender_percent_lower_limit = plugin.getConfig().getDouble("limits.defender.percent.lower-limit", (-1D)*Double.MAX_VALUE);
        defender_raw_upper_limit = plugin.getConfig().getDouble("limits.defender.raw.upper-limit", Double.MAX_VALUE);
        defender_raw_lower_limit = plugin.getConfig().getDouble("limits.defender.raw.lower-limit", (-1D)*Double.MAX_VALUE);

        Bukkit.getConsoleSender().sendMessage("    APUL: " + attacker_percent_upper_limit + "\n" +
                "    APLL: " + attacker_percent_lower_limit + "\n" +
                "    ARUL: " + attacker_raw_upper_limit + "\n" +
                "    ARLL: " + attacker_raw_lower_limit + "\n" +
                "    DPUL: " + defender_percent_upper_limit + "\n" +
                "    DPLL: " + defender_percent_lower_limit + "\n" +
                "    DRUL: " + defender_raw_upper_limit + "\n" +
                "    DRLL: " + defender_raw_lower_limit + "\n");
    }

    //plugin instance
    private final MMOItemsClassDamageStat plugin;

    //Attacker effective stat limits
    private double attacker_percent_upper_limit;
    private double attacker_percent_lower_limit;
    private double attacker_raw_upper_limit;
    private double attacker_raw_lower_limit;

    //Defender effective stat limits
    private double defender_percent_upper_limit;
    private double defender_percent_lower_limit;
    private double defender_raw_upper_limit;
    private double defender_raw_lower_limit;

    //This is meant to fire after all other PlayerAttackEvent handlers to show final damage (after built-in handlers)
    @EventHandler
    (
    ignoreCancelled = true,
    priority = EventPriority.MONITOR
    )
    public void finalAttackDamage(PlayerAttackEvent event){
        debugPrint(ChatColor.LIGHT_PURPLE + "Final Damage: " + event.getDamage().getDamage());

        //remove after we know what elements there are
        /*event.getPlayer().sendMessage(MythicLib.plugin.getElements().getAll().size() + "");

        Iterator<Element> elements = MythicLib.plugin.getElements().getAll().iterator();
        List<Element> elements2 = new ArrayList<Element>(MythicLib.plugin.getElements().getAll());

        event.getPlayer().sendMessage(elements2.size() + "");

        for(Element element : elements2){
            event.getPlayer().sendMessage(element.getId());
        }*/
    }

    //The built-in PlayerAttackEvent listeners are located in io.lumine.mythic.lib.listener.AttackEffects
    //Checks for the class damage stats and modifies damage accordingly
    @EventHandler
    public void OnPlayerAttack(PlayerAttackEvent event) {

        //If the hit entity is a player
        if (event.getEntity() instanceof Player) {
            Player defendingPlayer = (Player) event.getEntity();
            String defendingPlayerClass = PlayerData.get(defendingPlayer.getUniqueId()).getProfess().getId();

            Player attackingPlayer = event.getPlayer();
            String attackingPlayerClass = PlayerData.get(attackingPlayer.getUniqueId()).getProfess().getId();

            debugPrint(ChatColor.GREEN + "Detected attack by " + event.getPlayer().getName() + " against " + event.getEntity().getName());

            debugPrint(ChatColor.GREEN + "   Attacker " + attackingPlayer.getName() + " has class " + PlayerData.get(attackingPlayer.getUniqueId()).getProfess().getId());
            debugPrint(ChatColor.GREEN + "   Attacked " + defendingPlayer.getName() + " has class " + defendingPlayerClass);

            debugPrint(ChatColor.BLUE + "Unmodified Damage: " + event.getDamage().getDamage());

            //Get equipped items through RPGInventory
            //List<ItemStack> items = InventoryAPI.getActiveItems(event.getPlayer());
            //items.addAll(InventoryAPI.getPassiveItems(event.getPlayer()));

            // Initialize stats to zero so they can still be used in the calculation even if not present.
            double attackerRawStat = 0;
            double attackerPercentStat = 0;
            double defenderRawStat = 0;
            double defenderPercentStat = 0;

            //For each stat that is applied to the player THAT ATTACKED (the attacker)
            for (StatInstance si : (new PlayerStats(net.Indyuce.mmoitems.api.player.PlayerData.get(attackingPlayer.getUniqueId()))).getMap().getInstances()) {
                // If the stat has a value of greater than 0 (would have an effect)
                if(si.getTotal() != 0.0d) {
                    //Save flat damage stat
                    if (si.getStat().equals("RAW_DAMAGE_TO_" + defendingPlayerClass)) {
                        attackerRawStat = si.getTotal();
                        if(attackerRawStat > attacker_raw_upper_limit){
                            attackerRawStat = attacker_raw_upper_limit;
                        }
                        if(attackerRawStat < attacker_raw_lower_limit){
                            attackerRawStat = attacker_raw_lower_limit;
                        }
                    }

                    //Save percent damage stat
                    if (si.getStat().equals("PERCENT_DAMAGE_TO_" + defendingPlayerClass)) {
                        attackerPercentStat = si.getTotal();
                        if(attackerPercentStat > attacker_percent_upper_limit){
                            attackerPercentStat = attacker_percent_upper_limit;
                        }
                        if(attackerPercentStat < attacker_percent_lower_limit){
                            attackerPercentStat = attacker_percent_lower_limit;
                        }
                    }
                }
            }

            //for each stat that is applied to the player THAT WAS ATTACKED (the defender)
            for (StatInstance si : (new PlayerStats(net.Indyuce.mmoitems.api.player.PlayerData.get(defendingPlayer.getUniqueId())).getMap().getInstances())) {
                // If the stat has a value of greater than 0 (would have an effect)
                if(si.getTotal() != 0.0d) {
                    //Save flat damage stat
                    if (si.getStat().equals("RAW_DAMAGE_FROM_" + attackingPlayerClass)) {
                        defenderRawStat = si.getTotal();
                        if(defenderRawStat > defender_raw_upper_limit){
                            defenderRawStat = defender_raw_upper_limit;
                        }
                        if(defenderRawStat < defender_raw_lower_limit){
                            defenderRawStat = defender_raw_lower_limit;
                        }
                    }

                    //Save percent damage stat
                    if (si.getStat().equals("PERCENT_DAMAGE_FROM_" + attackingPlayerClass)) {
                        defenderPercentStat = si.getTotal();
                        if(defenderPercentStat > defender_percent_upper_limit){
                            defenderPercentStat = defender_percent_upper_limit;
                        }
                        if(defenderPercentStat < defender_percent_lower_limit){
                            defenderPercentStat = defender_percent_lower_limit;
                        }
                    }
                }
            }

            //TODO: use https://www.objecthunter.net/exp4j/ to get the formula from the config (substitute values first)

            // Apply the retrieved stats
            // (Base Damage + (AttackerRaw + DefenderRaw)) * (AttackerPercent * DefenderPercent)
            double valueToAdd = (attackerRawStat + defenderRawStat);
            //double valueToMultiply = ((1.0D + (attackerPercentStat / 100.0D)) * (1.0D + (defenderPercentStat / 100.0D)));
            double valueToMultiply = ((attackerPercentStat / 100.0D) + (defenderPercentStat / 100.0D));
            event.getDamage().add(valueToAdd);
            //event.getDamage().multiply(valueToMultiply);
            event.getDamage().additiveModifier(valueToMultiply); //TODO: make sure this is the correct method to use

            debugPrint(attackerPercentStat + ", " + defenderPercentStat);
            debugPrint("Value to Add: " + valueToAdd);
            debugPrint("Value to Multiply: " + valueToMultiply);

            debugPrint(ChatColor.BLUE + "Modified Damage: " + event.getDamage().getDamage());
        }
    }
}
