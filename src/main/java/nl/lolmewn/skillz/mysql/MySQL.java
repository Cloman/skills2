package nl.lolmewn.skillz.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.Main;

/**
 * @author Lolmewn
 */
public class MySQL {
    
    private Main plugin;
    private boolean isFault = false;
    
    public MySQL(Main main){
        plugin = main;
    }
    
    public boolean isFault(){
        return isFault;
    }
    
    public Connection getConnection() throws SQLException{
        if(plugin.getSettings().isUsingStats()){
            return plugin.getStatsApi().getConnection();
        }
        //TODO make own connection
        return null;
    }

    public void checkTables() {
        try {
            Connection con = this.getConnection();
            con.createStatement().executeQuery("CREATE TABLE IF NOT EXISTS ");
        } catch (SQLException ex) {
            this.isFault = true;
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void checkIndexes() {
        try {
            Connection con = this.getConnection();
        } catch (SQLException ex) {
            this.isFault = true;
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
