package nl.lolmewn.skillz.api.events;

import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Lolmewn
 */
public class SkillzPlayerLevelupEvent extends Event{
    
    private static final HandlerList handlers = new HandlerList();
    private final SkillzPlayer player;
    private final Skill skill;
    private int newLevel;
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public SkillzPlayerLevelupEvent(SkillzPlayer player, Skill skill, int newLevel){
        this.player = player;
        this.skill = skill;
        this.newLevel = newLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public SkillzPlayer getPlayer() {
        return player;
    }

    public Skill getSkill() {
        return skill;
    }
    
    public void setNewLevel(int level){
        this.newLevel = level;
    }

}
