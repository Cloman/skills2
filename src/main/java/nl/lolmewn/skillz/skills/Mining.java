/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */
package nl.lolmewn.skillz.skills;

import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.SkillzApi;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import nl.lolmewn.skillz.util.MathProcessor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Mining extends Skill {

    private Random r;
    
    public Mining(SkillzApi api) {
        super(api, "Mining");
        if (this.isEnabled()) {
            api.getPlugin().getServer().getPluginManager().registerEvents(this, api.getPlugin());
        }
    }

    @Override
    public long getMultiplier() {
        return this.getSkillConfig().getInt("multiplier", 1);
    }

    @Override
    public boolean isEnabled() {
        return this.getSkillConfig().getBoolean("enabled", true);
    }

    @Override
    public void setEnabled(boolean value) {
        if(value){
            this.getAPI().getPlugin().getServer().getPluginManager().registerEvents(this, this.getAPI().getPlugin());
        }else{
            BlockBreakEvent.getHandlerList().unregister(this);
        }
        try {
            this.getSkillConfig().set("enabled", value);
            this.saveSkillConfig();
        } catch (IOException ex) {
            Logger.getLogger(Mining.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getMoneyReward() {
        return this.getSkillConfig().getInt("rewards.money", this.getAPI().getSettings().getDefaultMoneyReward());
    }

    @Override
    public boolean checkLevelup(SkillzPlayer player) {
        int level = player.getLevel(this);
        if(level == 0){
            return true;
        }
        double xp = player.getXP(this);
        return xp / (level * level * 10) >= 1;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (!this.getSkillConfig().contains("blocks." + event.getBlock().getType().toString())
                && !this.getSkillConfig().contains("blocks.tool_level." + event.getPlayer().getItemInHand().getType().toString())) {
            return;
        }
        SkillzPlayer player = this.getAPI().getPlayerManager().getPlayer(event.getPlayer().getName());
        if (this.getSkillConfig().getInt("blocks.tool_level." + event.getPlayer().getItemInHand().getType().toString(), 0) > player.getLevel(this)) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    this.getMessage("messages.needsHigherLevelTool",
                            ChatColor.RED + "You need a higher %name% level to use this tool! "
                            + "Level needed is %level%").replace("%name%", this.getName())
                            .replace("%level%", "" + this.getSkillConfig().getInt("blocks." + event.getBlock().getType().toString() + ".level"))));
            event.setCancelled(true);
            return;
        }
        if (this.getSkillConfig().getInt("blocks." + event.getBlock().getType().toString() + ".level", 0) > player.getLevel(this)) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    this.getMessage("messages.needsHigherLevelBlock",
                            ChatColor.RED + "You need a higher %name% level to break this block! "
                            + "Level needed is %level%")
                    .replace("%name%", this.getName())
                    .replace("%level%", "" + this.getSkillConfig().getInt("blocks." + event.getBlock().getType().toString() + ".level"))));
            event.setCancelled(true);
            return;
        }
        player.addXP(this, this.getSkillConfig().getInt("blocks." + event.getBlock().getType().toString() + ".xp", 0));
        if(this.getSkillConfig().getBoolean("special.doubleDrops.enabled", false)){
            handleDoubleDrop(player, event);
        }
        if(this.getSkillConfig().getBoolean("special.doubleXp.enabled", false) && event.getExpToDrop() != 0){
            handleDoubleXp(player, event);
        }
    }

    @Override
    public void levelUp(SkillzPlayer player, int newLevel) {
        super.defaultLevelUp(player, newLevel);
    }
    
    private void handleDoubleDrop(SkillzPlayer player, BlockBreakEvent event) {
        if(r == null){
            r = new Random();
        }
        if(r.nextDouble() * 100 < 
                MathProcessor.processEquation(this.getMessage("special.doubleDrops.chance", "%level%/3")
                .replace("%level%", "" + player.getLevel(this))
                .replace("%xp%", "" + player.getXP(this)))){
            if(this.getSkillConfig().getBoolean("special.doubleDrops.notifyPlayer", true)){
                event.getPlayer().sendMessage(
                        ChatColor.translateAlternateColorCodes('&', 
                            this.getMessage("messages.doubleDrop", 
                                "Your block had a double drop! How luck you are.")));
            }
            Collection<ItemStack> drops = event.getBlock().getDrops(event.getPlayer().getItemInHand());
            for(ItemStack stack : drops){
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
            }
        }
    }
    
    private void handleDoubleXp(SkillzPlayer player, BlockBreakEvent event) {
        if(r == null){
            r = new Random();
        }
        if(r.nextDouble() * 100 < 
                MathProcessor.processEquation(this.getMessage("special.doubleXp.chance", "%level%")
                .replace("%level%", "" + player.getLevel(this))
                .replace("%xp%", "" + player.getXP(this)))){
            if(this.getSkillConfig().getBoolean("special.doubleDrops.notifyPlayer", true)){
                event.getPlayer().sendMessage(
                        ChatColor.translateAlternateColorCodes('&', 
                            this.getMessage("messages.doubleXp", 
                                "Your block dropped double XP! How luck you are.")));
            }
            event.setExpToDrop(event.getExpToDrop() * 2);
        }
    }
}
