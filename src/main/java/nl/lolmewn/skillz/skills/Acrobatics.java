package nl.lolmewn.skillz.skills;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.SkillzApi;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * @author Lolmewn
 */
public class Acrobatics extends Skill {

    public Acrobatics(SkillzApi api) {
        super(api, "Acrobatics");
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
            Logger.getLogger(Acrobatics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getMoneyReward() {
        return this.getSkillConfig().getInt("rewards.money", this.getAPI().getSettings().getDefaultMoneyReward());
    }

    @Override
    public boolean checkLevelup(SkillzPlayer player) {
        int level = player.getLevel(this);
        if (level * level * 10 >= player.getXP(this)) {
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (!event.getEntityType().equals(EntityType.PLAYER)) {
            return;
        }
        if (!event.getCause().equals(DamageCause.FALL)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (!player.hasPermission(this.getPermissionNode())) {
            return;
        }
        SkillzPlayer sPlayer = this.getAPI().getPlayerManager().getPlayer(player.getName());
        sPlayer.addXP(this, event.getDamage());
        if (this.getSkillConfig().getBoolean("special.avoidDamage.enabled", false)) {
            handleAvoidDamage(event, sPlayer, player);
        }
    }

    @Override
    public void levelUp(SkillzPlayer player, int newLevel) {
        super.defaultLevelUp(player, newLevel);
    }

    private void handleAvoidDamage(EntityDamageEvent event, SkillzPlayer sPlayer, Player player) {
        int avoidDamageMax = sPlayer.getLevel(this) / this.getSkillConfig().getInt("special.avoidDamage.perLevels", 10);
        double avoidedDamage = avoidDamageMax > event.getDamage() ? event.getDamage() : avoidDamageMax;
        if (player.getHealth() <= event.getDamage() - avoidedDamage
                && this.getSkillConfig().getBoolean("special.avoidDamage.ignoreOnDeath", true)) {
            return;
        }
        if (this.getSkillConfig().getBoolean("special.avoidDamage.notifyPlayer", true)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    this.getMessage("messages.damageAvoided", "You jumped smoothly, avoiding %avoidedDamage% damage!").replace("%avoidedDamage%", "" + avoidedDamage).replace("%player%", sPlayer.getPlayerName()).replace("%name%", this.getName())));
        }
        event.setDamage(event.getDamage() - avoidedDamage);
    }
}