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
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Woodcutting extends Skill {

    private Random r;
    
    public Woodcutting(SkillzApi api) {
        super(api, "Woodcutting");
        if(this.isEnabled()){
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
        
        if (!event.getPlayer().hasPermission(this.getPermissionNode())) {
            return;
        }
        
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE){
            return;
        }
        
        if (!this.getSkillConfig().contains("blocks." + event.getBlock().getType().toString())
                && !this.getSkillConfig().contains("blocks." + event.getBlock().getType().name() + ":" + event.getBlock().getData())
                && !this.getSkillConfig().contains("blocks.tool_level." + event.getPlayer().getInventory().getItemInMainHand().getType().toString())) {
            return;
        }
        ConfigurationSection sec = this.getSkillConfig().contains("blocks." + event.getBlock().getType().name() + ":" + event.getBlock().getData()) ? 
                this.getSkillConfig().getConfigurationSection("blocks." + event.getBlock().getType().name() + ":" + event.getBlock().getData()) :
                this.getSkillConfig().getConfigurationSection("blocks." + event.getBlock().getType().name());
        SkillzPlayer player = this.getAPI().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (this.getSkillConfig().getInt("blocks.tool_level." + event.getPlayer().getInventory().getItemInMainHand().getType().toString(), 0) > player.getLevel(this)) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    this.getMessage("messages.needsHigherLevelTool",
                            ChatColor.RED + "You need a higher %name% level to use this tool! "
                            + "Level needed is %level%").replace("%name%", this.getName())
                            .replace("%level%", "" + this.getSkillConfig().getInt("blocks.tool_level." + event.getPlayer().getInventory().getItemInMainHand().getType().toString(), 0))));
            event.setCancelled(true);
            return;
        }
        if(sec == null){
            return;
        }
        if (sec.getInt("level", 0) > player.getLevel(this)) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    this.getMessage("messages.needsHigherLevelBlock",
                            ChatColor.RED + "You need a higher %name% level to break this block! "
                            + "Level needed is %level%")
                    .replace("%name%", this.getName())
                    .replace("%level%", "" + sec.getInt("level"))));
            event.setCancelled(true);
            return;
        }
        player.addXP(this, sec.getInt("xp", 0));
        if (this.getSkillConfig().getBoolean("special.doubleDrops.enabled", false)) {
            handleDoubleDrop(player, event);
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
            Collection<ItemStack> drops = event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand());
            for(ItemStack stack : drops){
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
            }
        }
    }
}
