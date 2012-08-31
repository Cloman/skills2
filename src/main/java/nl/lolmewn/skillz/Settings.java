/*
 * Copyright 2012 Lolmewn <info@lolmewn.nl>
 */

package nl.lolmewn.skillz;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Lolmewn
 */
public class Settings {
    
    private Main plugin;
    
    private double version;
    private boolean update;

    public Settings(Main aThis) {
        this.plugin = aThis;
        this.loadSettings();
    }
    
    private Main getPlugin(){
        return this.plugin;
    }

    protected final void loadSettings() {
        if(!new File(this.getPlugin().getDataFolder(), "config.yml").exists()){
            this.getPlugin().saveResource("config.yml", false);
        }
        FileConfiguration f = this.getPlugin().getConfig();
        this.version = f.getDouble("version");
        this.update = f.getBoolean("update", true);
    }

    public boolean isUpdate() {
        return update;
    }

    public double getVersion() {
        return version;
    }
    
}
