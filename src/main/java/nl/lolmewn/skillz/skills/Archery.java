package nl.lolmewn.skillz.skills;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.SkillzApi;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import nl.lolmewn.skillz.util.MathProcessor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @author Lolmewn
 */
public class Archery extends Skill {

    private Random r;
    private long xp;

    public Archery(SkillzApi api) {
        super(api, "Archery");
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void archery(EntityDamageByEntityEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }
        //TODO add worldguard checks
        //TODO npc checks
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }
        Player player = ((Player) arrow.getShooter());
        if (!player.hasPermission(this.getPermissionNode())) {
            return;
        }
        LivingEntity damagee = (LivingEntity) event.getEntity();
        SkillzPlayer sPlayer = this.getAPI().getPlayerManager().getPlayer(player.getUniqueId());
        xp = 1;
        if (this.getSkillConfig().getBoolean("special.rangeXpMultiplier.enabled", false)) {
            double distance = player.getLocation().distanceSquared(arrow.getLocation());
            xp = (long) distance / this.getSkillConfig().getLong("spcial.rangeXpMultiplier.perBlocks", 10L);
            if (xp < 1 && !this.getSkillConfig().getBoolean("special.rangeXpMultiplier.under1xpPossible", false)) {
                xp = 1;
            }
        }
        if (this.getSkillConfig().getBoolean("special.headshot.enabled", false)) {
            handleHeadhot(damagee, event, arrow);
        }
        sPlayer.addXP(this, xp);
        if (this.getSkillConfig().getBoolean("special.damageMultiplier.enabled", false)) {
            handleDamageMultiplier(player, sPlayer, damagee, event);
        }
    }

    private void handleDamageMultiplier(Player player, SkillzPlayer sPlayer, LivingEntity damagee, EntityDamageByEntityEvent event) {
        if (this.getSkillConfig().getBoolean("special.damageMultiplier.ignoreOnPlayers", false) && damagee instanceof Player) {
            return;
        }
        double chance = MathProcessor.processEquation(this.getMessage("special.damageMultiplier.chance", "%level%/3").replace("%level%", "" + sPlayer.getLevel(this)).replace("%xp%", "" + sPlayer.getXP(this)));
        if (r == null) {
            r = new Random();
        }
        if (r.nextDouble() * 100 <= chance) {
            event.setDamage((int) (event.getDamage() * this.getSkillConfig().getDouble("special.damageMultiplier.multiplier", 1.5)));
            if (this.getSkillConfig().getBoolean("special.damageMultiplier.notify", true)) {
                player.sendMessage(this.getMessage("messages.damageMultiplied",
                        "You shot with extreme power, dealing extra damage!").replace("%player%", player.getName()).replace("%damagee%", damagee instanceof Player ? ((Player) damagee).getName() : damagee.getType().getName().toLowerCase()).replace("%newdamage%", "" + event.getDamage()));
            }
        }
    }

    private void handleHeadhot(LivingEntity damagee, EntityDamageByEntityEvent event, Arrow arrow) {
        if (this.getSkillConfig().getBoolean("special.headshot.ignoreSlimes", true) && damagee instanceof Slime) {
            return;
        }
        if (damagee.getEyeLocation().distance(arrow.getLocation()) >= 0.4) {
            return;
        }
        xp *= this.getSkillConfig().getLong("special.headshot.xpMultiplier", (long) 1.5);
        int damage = (int) (event.getDamage() * this.getSkillConfig().getDouble("special.headshot.damageMultiplier", 2));
        event.setDamage(damage);
    }
}
