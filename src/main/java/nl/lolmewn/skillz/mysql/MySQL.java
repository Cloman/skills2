package nl.lolmewn.skillz.mysql;

import java.sql.Connection;
import nl.lolmewn.skillz.Main;

/**
 * @author Lolmewn
 */
public class MySQL {
    
    private Main plugin;
    
    public MySQL(Main main){
        plugin = main;
    }
    
    public Connection getConnection(){
        if(plugin.getSettings().isUsingStats()){
            return plugin.getStatsApi().getConnection();
        }
        //make own connection
        return null;
    }

}
