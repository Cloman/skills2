package nl.lolmewn.skillz.api;

import java.util.HashMap;

/**
 * @author Lolmewn
 */
public class SkillManager extends HashMap<String, Skill>{
    
    public void add(Skill skill){
        this.put(skill.getName(), skill);
    }

}
