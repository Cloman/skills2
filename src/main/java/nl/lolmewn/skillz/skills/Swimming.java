package nl.lolmewn.skillz.skills;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.SkillzApi;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import nl.lolmewn.skillz.util.MathProcessor;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Lolmewn
 */
public class Swimming extends Skill{
    
    private Random r;
    
    public Swimming(SkillzApi api){
        super(api, "Swimming");
        if (this.isEnabled()) {
            api.getPlugin().getServer().getPluginManager().registerEvents(this, api.getPlugin());
        }
    }
    
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
            EntityDamageEvent.getHandlerList().unregister(this);
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
        if (xp / (level * level * 10) >= 1) {
            return true;
        }
        return false;
    }

    @Override
    public void levelUp(SkillzPlayer player, int newLevel) {
        super.defaultLevelUp(player, newLevel);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDamage(EntityDamageEvent event){
        if (!this.isEnabled()) {
            return;
        }
        if(!event.getEntityType().equals(EntityType.PLAYER)){
            return;
        }
        if(!event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)){
            return;
        }
        Player player = (Player)event.getEntity();
        if(!player.hasPermission(this.getPermissionNode())){
            return;
        }
        SkillzPlayer sPlayer = this.getAPI().getPlayerManager().getPlayer(player.getName());
        sPlayer.addXP(this, event.getDamage());
        if(this.getSkillConfig().getBoolean("special.ignoreDamage.enabled", false)){
            handleAvoidDamage(event, sPlayer, player);
        }
    }

    private void handleAvoidDamage(EntityDamageEvent event, SkillzPlayer sPlayer, Player player) {
        if(r == null){
            r = new Random();
        }
        if(r.nextDouble() * 100 < 
                MathProcessor.processEquation(this.getMessage("special.ignoreDamage.chance", "%level%/2")
                .replace("%level%", "" + sPlayer.getLevel(this))
                .replace("%xp%", "" + sPlayer.getXP(this)))){
            if(this.getSkillConfig().getBoolean("special.ignoreDamage.notifyPlayer", true)){
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', 
                            this.getMessage("messages.ignoreDamage", 
                                "You swum extra hard, causing you not to loose hearts!")));
            }
            event.setCancelled(true);
        }
    }

}
