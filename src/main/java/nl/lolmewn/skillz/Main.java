package nl.lolmewn.skillz;

import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Lolmewn
 */
public class Main extends JavaPlugin{
    
    private Settings settings;
    private UserManager userManager;
    
    protected double newVersion = 0;
    
    @Override
    public void onEnable(){
        this.settings = new Settings(this);
        this.userManager = new UserManager(this);
    }
    
    @Override
    public void onDisable(){
        this.getUserManager().saveAllUsers();
        if(this.newVersion != 0){
            //Set new version in the config
            this.getConfig().set("version", this.newVersion);
            this.saveConfig();
        }
    }
    
    public Settings getSettings(){
        return this.settings;
    }
    
    public UserManager getUserManager(){
        return this.userManager;
    }
    
    public double getVersion(){
        return this.getSettings().getVersion();
    }
    
}
