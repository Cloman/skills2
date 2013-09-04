package nl.lolmewn.skillz.players;

import java.util.HashMap;
import nl.lolmewn.skillz.Main;

/**
 * @author Lolmewn
 */
public class PlayerManager extends HashMap<String, SkillzPlayer>{

    private Main plugin;
    
    public PlayerManager(Main aThis) {
        plugin = aThis;
    }
    
    public SkillzPlayer getPlayer(String name){
        if(this.containsKey(name)){
            return this.get(name);
        }
        this.loadPlayer(name);
        return this.get(name);
    }

    public void loadPlayer(String name) {
        //TODO load player
    }
    
    public void savePlayer(String name, boolean remove){
        //TODO save player
        if(remove){
            this.remove(name);
        }
    }

}
