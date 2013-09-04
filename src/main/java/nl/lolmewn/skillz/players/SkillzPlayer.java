package nl.lolmewn.skillz.players;

import java.util.HashMap;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.api.events.SkillzPlayerLevelupEvent;
import nl.lolmewn.skillz.api.events.SkillzPlayerXPGainEvent;

/**
 * @author Lolmewn
 */
public class SkillzPlayer {
    
    private String name;
    
    private HashMap<String, Double> xpValues = new HashMap<String, Double>();
    private HashMap<String, Integer> lvlValues = new HashMap<String, Integer>();
    
    public SkillzPlayer(String name){
        this.name = name;
    }
    
    public String getPlayerName(){
        return name;
    }
    
    public boolean addXP(Skill skill){
        return this.addXP(skill, 1);
    }
    
    public boolean addXP(Skill skill, double value){
        double currValue = this.getXP(skill);
        value = value * skill.getMultiplier(); 
        SkillzPlayerXPGainEvent event = new SkillzPlayerXPGainEvent(this, skill, currValue + value);
        skill.getAPI().getPlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()){
            return false;
        }
        this.setXP(skill, event.getNewAmount());
        return skill.checkLevelup(this);
    }

    public double getXP(Skill skill) {
        if(this.xpValues.containsKey(skill.getName())){
            return this.xpValues.get(skill.getName());
        }
        return 0;
    }

    public int getLevel(Skill skill) {
        if(this.lvlValues.containsKey(skill.getName())){
            return this.lvlValues.get(skill.getName());
        }
        return 0;
    }
    
    /**
     * Levels this skill up
     * @param skill Skill to level up
     * @param amount amount to level up
     * @return new level
     */
    public int levelUp(Skill skill, int amount){
        SkillzPlayerLevelupEvent event = new SkillzPlayerLevelupEvent(this, skill, this.getLevel(skill) + amount);
        skill.getAPI().getPlugin().getServer().getPluginManager().callEvent(event);
        this.lvlValues.put(skill.getName(), event.getNewLevel());
        return event.getNewLevel();
    }
    
    public int levelUp(Skill skill){
        return this.levelUp(skill, 1);
    }

    public void setLevel(Skill skill, int newLevel) {
        this.lvlValues.put(skill.getName(), newLevel);
    }
    
    public void setXP(Skill skill, double xp){
        this.xpValues.put(skill.getName(), xp);
    }

}
