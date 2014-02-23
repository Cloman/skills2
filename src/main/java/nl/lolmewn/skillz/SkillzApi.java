package nl.lolmewn.skillz;

import java.util.Collection;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.PlayerManager;

/**
 * @author Lolmewn
 */
public class SkillzApi {
    
    private final Main main;
    
    protected SkillzApi(Main main){
        this.main = main;
    }
    
    public Main getPlugin(){
        return main;
    }

    public PlayerManager getPlayerManager() {
        return main.getPlayerManager();
    }
    
    public Settings getSettings(){
        return main.getSettings();
    }
    
    public MessageManager getMessageManager(){
        return main.getMessageManager();
    }
    
    public void addSkill(Skill skill){
        this.main.getSkillManager().add(skill);
    }
    
    public Collection<Skill> getSkills(){
        return this.main.getSkillManager().values();
    }
    
    public void deleteSkill(String skillName){
        this.main.getSkillManager().remove(skillName);
    }

}
