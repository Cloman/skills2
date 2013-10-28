package nl.lolmewn.skillz.api.events;

import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Lolmewn
 */
public class SkillzPlayerXPGainEvent extends Event implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final SkillzPlayer player;
    private final Skill skill;
    private final double newAmount;
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @Override
    public boolean isCancelled(){
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean value){
        this.cancelled = value;
    }
    
    public SkillzPlayerXPGainEvent(SkillzPlayer player, Skill skill, double newAmount){
        this.player = player;
        this.skill = skill;
        this.newAmount = newAmount;
    }

    public double getNewAmount() {
        return newAmount;
    }
    
    public double getOldAmount(){
        return player.getXP(skill);
    }

    public SkillzPlayer getPlayer() {
        return player;
    }

    public Skill getSkill() {
        return skill;
    }

}
