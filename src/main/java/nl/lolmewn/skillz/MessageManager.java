package nl.lolmewn.skillz;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Lolmewn
 */
public class MessageManager {
    
    private final FileConfiguration file;
    private final Main plugin;
    private final HashSet<String> warned = new HashSet<String>();
    
    public MessageManager(Main m, FileConfiguration file){
        this.plugin = m;
        this.file = file;
    }
    
    public String getMessage(String path, String def){
        String get = file.getString(path);
        if(get == null && !warned.contains(path)){
            plugin.getLogger().warning("Message for " + path + " not found in messages.yml, using default." );
            warned.add(path);
        }else if(get == null){
            return def;
        }
        return get;
    }
    
    public String getColoredMessage(String path, String def){
        return ChatColor.translateAlternateColorCodes('&', this.getMessage(path, def));
    }
    
    public FileConfiguration getFileConfiguration(){
        return this.file;
    }

}
