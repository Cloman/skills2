package nl.lolmewn.skillz.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.Main;

/**
 * @author Lolmewn
 */
public class MySQL {
    
    private final Main plugin;
    private boolean isFault = false;
    
    public MySQL(Main main){
        plugin = main;
    }
    
    public boolean isFault(){
        return isFault;
    }
    
    public Connection getConnection() throws SQLException{
//        if(plugin.getSettings().isUsingStats()){
//            return plugin.getStatsApi().getConnection();
//        }
        //TODO make own connection
        return null;
    }

    public void checkTables() {
        try {
            Connection con = this.getConnection();
            con.createStatement().executeQuery("CREATE TABLE IF NOT EXISTS " + this.plugin.getSettings().getDatabasePrefix() + "players "
                    + "(player varchar(255) NOT NULL,"
                    + "skill varchar(255) NOT NULL,"
                    + "xp double NOT NULL,"
                    + "level int NOT NULL,"
                    + "UNIQUE KEY 'no_duplicates' ('player', 'skill'))");
            con.close();
        } catch (SQLException ex) {
            this.isFault = true;
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void checkIndexes() {
        try {
            Connection con = this.getConnection();
            con.setAutoCommit(true);
            Statement st = con.createStatement();
            ResultSet set;
            if((set = st.executeQuery("SELECT ENGINE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME LIKE '" + this.plugin.getSettings().getDatabasePrefix() + "%'")).next()){
                if(set.getString("ENGINE").equalsIgnoreCase("InnoDB")){
                    set.close();
                    st.execute("SET SESSION old_alter_table=1");
                }
            }
             //for InnoDB
            if(!st.executeQuery("SHOW INDEXES FROM " + this.plugin.getSettings().getDatabasePrefix() + "players WHERE Key_name='no_duplicates'").next()){
                st.execute("ALTER IGNORE TABLE " + this.plugin.getSettings().getDatabasePrefix() + "players ADD UNIQUE INDEX no_duplicates (player, skill)");
            }
            st.close();
            con.close();
        } catch (SQLException ex) {
            this.isFault = true;
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
