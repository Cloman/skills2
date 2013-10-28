package nl.lolmewn.skillz.skills;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.SkillzApi;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Sybren
 */
public class Swords extends Skill{
    
    private double xp;

    public Swords(SkillzApi api){
        super(api, "Swords");
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
            EntityDamageByEntityEvent.getHandlerList().unregister(this);
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
    
    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event){
        if(!event.getDamager().getType().equals(EntityType.PLAYER)){
            return;
        }
        Player damager = (Player)event.getDamager();
        if(!damager.hasPermission(this.getPermissionNode())){
            return;
        }
        SkillzPlayer player = this.getAPI().getPlayerManager().getPlayer(damager.getName());
        ItemStack inHand = damager.getItemInHand();
        Material type = inHand.getType();
        if(!type.equals(Material.WOOD_SWORD) 
                && !type.equals(Material.IRON_SWORD) 
                && !type.equals(Material.STONE_SWORD) 
                && !type.equals(Material.DIAMOND_SWORD) 
                && !type.equals(Material.GOLD_SWORD)){
            return;
        }
        xp = event.getDamage();
        
        
        player.addXP(this, xp);
    }

}
