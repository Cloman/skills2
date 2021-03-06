package nl.lolmewn.skillz.players;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.Main;
import nl.lolmewn.skillz.api.Skill;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Lolmewn
 */
public class PlayerManager extends HashMap<UUID, SkillzPlayer> {

    private final Main plugin;
    private final HashSet<UUID> beingLoaded = new HashSet<UUID>();

    public PlayerManager(Main aThis) {
        plugin = aThis;
    }

    public SkillzPlayer getPlayer(UUID uuid) {
        if (this.containsKey(uuid)) {
            return this.get(uuid);
        }
        this.loadPlayer(uuid);
        return this.get(uuid);
    }

    public void loadPlayer(final UUID uuid) {
        this.beingLoaded.add(uuid);
        final SkillzPlayer temp = new SkillzPlayer(uuid);

        if (plugin.getSettings().isUseMySQL()) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        Connection con = plugin.getMySQL().getConnection();
                        PreparedStatement st = con.prepareStatement(
                                "SELECT * FROM " + plugin.getSettings().getDatabasePrefix() + "players"
                                + "WHERE player=?");
                        st.setString(1, uuid.toString());
                        ResultSet set = st.executeQuery();
                        if (set != null) {
                            while (set.next()) {
                                String skillName = set.getString("skill");
                                skillName = skillName.substring(0, 1).toUpperCase() + skillName.substring(1).toLowerCase();
                                Skill skill = plugin.getSkillManager().get(skillName);
                                if (skill == null) {
                                    plugin.getLogger().warning("Couldn't load skill " + skillName + " for player " + uuid + ", skill not found. Use /skills cleanup to clean all players from non-existing skills");
                                    continue;
                                }
                                temp.setXP(skill, set.getDouble("xp"));
                                temp.setLevel(skill, set.getInt("level"));
                            }
                            set.close();
                        }
                        beingLoaded.remove(uuid);
                        put(uuid, temp);
                        st.close();
                        con.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } else {
            ConfigurationSection section = plugin.getPlayerFileConfiguration().getConfigurationSection(uuid.toString());
            if (section != null) {
                for (String skillName : section.getKeys(false)) {
                    skillName = skillName.substring(0, 1).toUpperCase() + skillName.substring(1).toLowerCase();
                    Skill skill = plugin.getSkillManager().get(skillName);
                    if (skill == null) {
                        plugin.getLogger().warning("Couldn't load skill " + skillName + " for player " + uuid + ", skill not found. Use /skills cleanup to clean all players from non-existing skills");
                        continue;
                    }
                    temp.setXP(skill, section.getDouble(skillName + ".xp"));
                    temp.setLevel(skill, section.getInt(skillName + ".level"));
                }
            }
            this.beingLoaded.remove(uuid);
        }
        this.put(uuid, temp);
    }

    public void savePlayer(final UUID uuid, boolean remove) {
        final SkillzPlayer player = this.get(uuid);
        if (player == null) {
            plugin.getLogger().severe("Attempted to save player with uuid " + uuid.toString() + ", but player object not found!");
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
                        st.setString(1, uuid.toString());
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
            ConfigurationSection section = plugin.getPlayerFileConfiguration().getConfigurationSection(uuid.toString());
            if(section == null){
                section = plugin.getPlayerFileConfiguration().createSection(uuid.toString());
            }
            for (String skill : player.getSkills()) {
                section.set(skill + ".xp", player.getXP(skill));
                section.set(skill + ".level", player.getLevel(skill));
            }
            try {
                plugin.getPlayerFileConfiguration().save(new File(plugin.getDataFolder(), "users.yml"));
            } catch (IOException ex) {
                Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (remove) {
            this.remove(uuid);
        }
    }
}
