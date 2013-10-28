package nl.lolmewn.skillz;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * @author Lolmewn
 */
public class Settings {

    private final Main plugin;
    private boolean useMySQL;
    private int defaultMoneyReward;
    private ItemStack[] defaultItemRewards;
    private final boolean usingStats;
    private boolean broadcastLevelup;
    private String databasePrefix, databaseUser, databasePassword, databaseHost;
    private int databasePort;

    public Settings(Main main) {
        this.plugin = main;
        this.usingStats = main.getServer().getPluginManager().getPlugin("Stats") != null;
    }

    public void loadSettings() {
        plugin.saveDefaultConfig();
        FileConfiguration c = plugin.getConfig();
        this.defaultMoneyReward = c.getInt("defaultRewards.money", 0);
        this.broadcastLevelup = c.getBoolean("broadcastLevelup", true);
        this.loadItems();
    }

    public ItemStack[] getDefaultItemRewards() {
        return defaultItemRewards;
    }

    public int getDefaultMoneyReward() {
        return defaultMoneyReward;
    }

    public boolean isUseMySQL() {
        return useMySQL;
    }

    public boolean isUsingStats() {
        return usingStats;
    }

    public boolean isBroadcastLevelup() {
        return broadcastLevelup;
    }

    public String getDatabasePrefix() {
        return databasePrefix;
    }

    protected String getDatabaseUser() {
        return databaseUser;
    }

    protected String getDatabasePassword() {
        return databasePassword;
    }

    protected String getDatabaseHost() {
        return databaseHost;
    }

    protected int getDatabasePort() {
        return databasePort;
    }

    private void loadItems() {
        String items = plugin.getConfig().getString("defaultRewards.items", "");
        if (!items.equals("")) {
            if (items.contains(";")) {
                String[] split = items.split(";");
                this.defaultItemRewards = new ItemStack[split.length];
                for (int i = 0; i < split.length; i++) {
                    String item = split[i];
                    if (!item.contains(",")) {
                        plugin.getLogger().warning("Unable to load default item '" + item + "', no amount set");
                    } else {
                        try {
                            String itemName = item.split(",")[0];
                            Material m = Material.matchMaterial(itemName);
                            if (m == null) {
                                plugin.getLogger().severe("Material with name " + itemName + " not found");
                                continue;
                            }
                            int amount = Integer.parseInt(item.split(",")[1]);
                            byte itemData = item.split(",").length == 3 ? Byte.parseByte(item.split(",")[2]) : 0;
                            this.defaultItemRewards[i] = new ItemStack(m, amount, itemData);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Unable to load default item '" + item + "', amount is no number");
                        }
                    }
                }
            } else {
                this.defaultItemRewards = new ItemStack[1];
                if (!items.contains(",")) {
                    plugin.getLogger().warning("Unable to load default item '" + items + "', no amount set");
                } else {
                    try {
                        String itemName = items.split(",")[0];
                        Material m = Material.matchMaterial(itemName);
                        if (m == null) {
                            plugin.getLogger().severe("Material with name " + itemName + " not found");
                            return;
                        }
                        int amount = Integer.parseInt(items.split(",")[1]);
                        byte itemData = items.split(",").length == 3 ? Byte.parseByte(items.split(",")[2]) : 0;
                        this.defaultItemRewards[0] = new ItemStack(m, amount, itemData);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Unable to load default item '" + items + "', amount is no number");
                    }
                }
            }
        }
    }
}
