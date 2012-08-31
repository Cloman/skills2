/*
 * Copyright 2012 Lolmewn <info@lolmewn.nl>
 */

package nl.lolmewn.skillz;

import java.util.HashMap;

/**
 *
 * @author Lolmewn
 */
public class User {

    private String name;
    
    private HashMap<String, Integer> xp = new HashMap<String, Integer>();
    private HashMap<String, Integer> levels = new HashMap<String, Integer>();
    
    public User(String name){
        this.name = name;
    }
    
    public String getName(){
        return this.name;
    }
    
    public int getXP(String skill){
        return this.xp.get(skill);
    }
    
    public int getLevel(String skill){
        return this.levels.get(skill);
    }
    
    /**
     * This method allows you to add XP to a certain skill.
     * @param skill the name of the skill you want to add XP to
     * @param amount The amount of XP you want to be added to the skill
     * @return the new amount of XP
     */
    public int addXP(String skill, int amount){
        return this.xp.put(skill, this.xp.get(skill) + amount);
    }
    
    /**
     * This method allows you to level up a certain skill.
     * @param skill the name of the skill you want to level up.
     * @return the new level.
     */
    public int addLevel(String skill){
        return this.xp.put(skill, this.levels.get(skill) + 1);
    }
    
    public void resetLevels(){
        this.levels.clear();
    }
    
    public void resetXP(){
        this.xp.clear();
    }
    
}
