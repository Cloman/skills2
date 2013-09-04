package nl.lolmewn.skillz;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * @author Lolmewn
 */
public class Settings {
    
    private Main plugin;
    
    private boolean useMySQL;
    private int defaultMoneyReward;
    private ItemStack[] defaultItemRewards;
    private boolean usingStats;
    private boolean broadcastLevelup;
    
    public Settings(Main main){
        this.plugin = main;
        this.usingStats = main.getServer().getPluginManager().getPlugin("Stats") != null;
    }
    
    public void loadSettings(){
        plugin.saveDefaultConfig();
        FileConfiguration c = plugin.getConfig();
        this.defaultMoneyReward = c.getInt("defaultRewards.money", 0);
        this.broadcastLevelup = c.getBoolean("broadcastLevelup", true);
        String items = c.getString("defaultRewards.items", "");
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
                            int itemId = Integer.parseInt(item.split(",")[0]);
                            int amount = Integer.parseInt(item.split(",")[1]);
                            byte itemData = item.split(",").length == 3 ? Byte.parseByte(item.split(",")[2]) : 0;
                            this.defaultItemRewards[i] = new ItemStack(itemId, amount, itemData);
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
                        int itemId = Integer.parseInt(items.split(",")[0]);
                        int amount = Integer.parseInt(items.split(",")[1]);
                        byte itemData = items.split(",").length == 3 ? Byte.parseByte(items.split(",")[2]) : 0;
                        this.defaultItemRewards[0] = new ItemStack(itemId, amount, itemData);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Unable to load default item '" + items + "', amount is no number");
                    }
                }
            }
        }
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

}
