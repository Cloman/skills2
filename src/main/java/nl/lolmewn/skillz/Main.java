/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */
package nl.lolmewn.skillz;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.api.SkillManager;
import nl.lolmewn.skillz.commands.SkillsCommand;
import nl.lolmewn.skillz.mysql.MySQL;
import nl.lolmewn.skillz.players.PlayerManager;
import nl.lolmewn.skillz.skills.Acrobatics;
import nl.lolmewn.skillz.skills.Archery;
import nl.lolmewn.skillz.skills.Digging;
import nl.lolmewn.skillz.skills.Mining;
import nl.lolmewn.skillz.skills.Swimming;
import nl.lolmewn.skillz.skills.Woodcutting;
import nl.lolmewn.stats.api.StatsAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Main extends JavaPlugin {

    private FileConfiguration userFile;
    private MessageManager messageManager;
    private PlayerManager pManager;
    private Settings settings;
    private final SkillManager skillManager = new SkillManager();
    private SkillzApi api;
    private StatsAPI statsApi;
    private MySQL mysql;
    
    @Override
    public void onEnable() {
        this.checkFiles();
        this.checkOldVersion();
        this.initMessageManager();
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.settings = new Settings(this);
        settings.loadSettings();
        if(settings.isUseMySQL()){
            this.mysql = new MySQL(this);
            this.mysql.checkTables();
            this.mysql.checkIndexes();
        }
        pManager = new PlayerManager(this);
        api = new SkillzApi(this);
        this.getServer().getServicesManager().register(SkillzApi.class, api, this, ServicePriority.Low);
        this.getServer().getPluginManager().registerEvents(new Events(this), this);
        this.getCommand("skills").setExecutor(new SkillsCommand(this));
        this.loadDefaultSkills();
    }
    
    @Override
    public void onDisable(){
        for(Player player : this.getServer().getOnlinePlayers()){
            this.getPlayerManager().savePlayer(player.getUniqueId(), true);
        }
    }

    private void checkOldVersion() {
        File oldConfig = new File(this.getDataFolder(), "skills.yml");
        if (oldConfig.exists()) {
            convertConfig();
            File userDir = new File(this.getDataFolder(), "players/");
            if (userDir.exists()) {
                for (File user : userDir.listFiles()) {
                    convertUser(user);
                }
            }
            oldConfig.delete();
        }
    }

    private void convertUser(File user) {
        this.getLogger().info("Converting user " + user.getName().split("\\.")[0]);
        try {
            String username = user.getName().split("\\.")[0]; //no extension
            FileInputStream in = new FileInputStream(user);
            DataInputStream dis = new DataInputStream(in);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains("#")) {
                    continue;
                }
                if (!strLine.contains("=") || !strLine.contains(";")) {
                    continue;
                }
                String[] first = strLine.split("=");
                String skill = first[0];
                skill = skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
                String[] second = first[1].split(";");
                int xp = Integer.parseInt(second[0]);
                int lvl = Integer.parseInt(second[1]);
                this.userFile.set(username + "." + skill + ".xp", xp);
                this.userFile.set(username + "." + skill + ".level", lvl);
            }
            userFile.save(new File(this.getDataFolder(), "users.yml"));
            dis.close();
            br.close();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkFiles() {
        this.getDataFolder().mkdirs();
        if(!new File(this.getDataFolder(), "users.yml").exists()){
            try {
                new File(this.getDataFolder(), "users.yml").createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.userFile = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "users.yml"));
        if(!new File(this.getDataFolder(), "config.yml").exists()){
            this.saveDefaultConfig();
        }
    }

    public PlayerManager getPlayerManager() {
        return pManager;
    }
    
    public Settings getSettings(){
        return settings;
    }
    
    public StatsAPI getStatsApi(){
        return this.statsApi;
    }
    
    public SkillManager getSkillManager(){
        return skillManager;
    }
    
    public MessageManager getMessageManager(){
        return messageManager;
    }
    
    public FileConfiguration getPlayerFileConfiguration(){
        return this.userFile;
    }
    
    public MySQL getMySQL(){
        return mysql;
    }
    
    private void loadDefaultSkills(){
        this.getSkillManager().add(new Acrobatics(api));
        this.getSkillManager().add(new Archery(api));
        this.getSkillManager().add(new Digging(api));
        this.getSkillManager().add(new Mining(api));
        this.getSkillManager().add(new Swimming(api));
        this.getSkillManager().add(new Woodcutting(api));
    }

    private void initMessageManager() {
        File msgFile = new File(this.getDataFolder(), "messages.yml");
        if(!msgFile.exists()){
            this.saveResource("messages.yml", true);
        }
        this.messageManager = new MessageManager(this, YamlConfiguration.loadConfiguration(msgFile));
    }

    private void convertConfig() {
        FileConfiguration old = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "skills_old.yml"));
        
    }
}
