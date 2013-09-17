package nl.lolmewn.skillz.players;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.Main;
import nl.lolmewn.skillz.api.Skill;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Lolmewn
 */
public class PlayerManager extends HashMap<String, SkillzPlayer> {

    private Main plugin;
    private HashSet<String> beingLoaded = new HashSet<String>();

    public PlayerManager(Main aThis) {
        plugin = aThis;
    }

    public SkillzPlayer getPlayer(String name) {
        if (this.containsKey(name)) {
            return this.get(name);
        }
        this.loadPlayer(name);
        return this.get(name);
    }

    public void loadPlayer(final String name) {
        this.beingLoaded.add(name);
        final SkillzPlayer temp = new SkillzPlayer(name);

        if (plugin.getSettings().isUseMySQL()) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        Connection con = plugin.getMySQL().getConnection();
                        PreparedStatement st = con.prepareStatement(
                                "SELECT * FROM " + plugin.getSettings().getDatabasePrefix() + "players"
                                + "WHERE player=?");
                        st.setString(1, name);
                        ResultSet set = st.executeQuery();
                        if (set != null) {
                            while (set.next()) {
                                Skill skill = plugin.getSkillManager().get(set.getString("skill"));
                                if (skill == null) {
                                    plugin.getLogger().warning("Couldn't load skill " + skill + " for player " + name + ", skill not found. Use /skills cleanup to clean all players from non-existing skills");
                                    continue;
                                }
                                temp.setXP(skill, set.getDouble("xp"));
                                temp.setLevel(skill, set.getInt("level"));
                            }
                            set.close();
                        }
                        beingLoaded.remove(name);
                        put(name, temp);
                        st.close();
                        con.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } else {
            ConfigurationSection section = plugin.getPlayerFileConfiguration().getConfigurationSection(name);
            for (String skillName : section.getKeys(false)) {
                Skill skill = plugin.getSkillManager().get(skillName);
                if (skill == null) {
                    plugin.getLogger().warning("Couldn't load skill " + skill + " for player " + name + ", skill not found. Use /skills cleanup to clean all players from non-existing skills");
                    continue;
                }
                temp.setXP(skill, section.getDouble(skillName + ".xp"));
                temp.setLevel(skill, section.getInt(skillName + ".level"));
            }
            this.beingLoaded.remove(name);
        }
        this.put(name, temp);
    }

    public void savePlayer(final String name, boolean remove) {
        final SkillzPlayer player = this.get(name);
        if (player == null) {
            plugin.getLogger().severe("Attempted to save player " + name + ", but player object not found!");
            return;
        }
        if (plugin.getSettings().isUseMySQL()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        Connection con = plugin.getMySQL().getConnection();
                        con.setAutoCommit(false);
                        PreparedStatement st = con.prepareStatement(
                                "INSERT INTO " + plugin.getSettings().getDatabasePrefix() + "players"
                                + "(player, skill, xp, level) VALUES (?, ?, ?, ?)"
                                + "ON DUPLLICATE KEY UPDATE xp=VALUES(xp), level=VALUES(level)");
                        st.setString(1, name);
                        for (String skill : player.getSkills()) {
                            st.setString(2, skill);
                            st.setDouble(3, player.getXP(skill));
                            st.setInt(4, player.getLevel(skill));
                            st.addBatch();
                        }
                        st.executeBatch();
                        con.commit();
                        st.close();
                        con.setAutoCommit(true);
                        con.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } else {
            ConfigurationSection section = plugin.getPlayerFileConfiguration().getConfigurationSection(name);
            for (String skill : player.getSkills()) {
                section.set(skill + ".xp", player.getXP(skill));
                section.set(skill + ".level", player.getLevel(skill));
            }
        }
        if (remove) {
            this.remove(name);
        }
    }
}
