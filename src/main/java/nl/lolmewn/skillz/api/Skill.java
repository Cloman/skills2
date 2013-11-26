package nl.lolmewn.skillz.api;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.skillz.MessageManager;
import nl.lolmewn.skillz.SkillzApi;
import nl.lolmewn.skillz.players.SkillzPlayer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * @author Lolmewn
 */
public abstract class Skill implements Listener {

    private final String name;
    private final SkillzApi api;
    private FileConfiguration skillConfig;
    private String fileName, filePath;

    private static int amount;

    public Skill(SkillzApi api, String name) {
        this.name = name;
        this.api = api;
        this.checkSkillConfig();
        amount++;
    }

    public static int getAmountOfSkills() {
        return amount;
    }

    public String getPermissionNode() {
        return "skillz.skill." + this.getName();
    }

    private void checkSkillConfig() {
        fileName = name.toLowerCase();
        filePath = api.getPlugin().getDataFolder().getPath() + File.separator + "skills" + File.separator + fileName + ".yml";
        File config = new File(filePath);
        if (!config.exists()) {
            try {
                if (api.getPlugin().getResource("skills/" + fileName + ".yml") != null) {
                    api.getPlugin().saveResource("skills/" + fileName + ".yml", true);
                    api.getPlugin().getLogger().info("Default file for " + fileName + " saved to " + filePath);
                } else {
                    api.getPlugin().getLogger().warning("Default file for " + fileName + " not found, creating empty file");
                    config.createNewFile();
                }
            } catch (IOException ex) {
                api.getPlugin().getLogger().severe("Couldn't create skillfile for " + fileName);
                Logger.getLogger(Skill.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        skillConfig = YamlConfiguration.loadConfiguration(config);
    }

    public String getName() {
        return name;
    }

    public SkillzApi getAPI() {
        return api;
    }

    public MessageManager getMessageManager() {
        return api.getPlugin().getMessageManager();
    }

    public String getMessage(String path, String def) {
        String message = this.getSkillConfig().getString(path);
        if (message != null) {
            return message;
        }
        return this.getMessageManager().getMessage(path.replace("messages.", ""), def);
    }

    public FileConfiguration getConfig() {
        return getAPI().getPlugin().getConfig();
    }

    public FileConfiguration getSkillConfig() {
        return this.skillConfig;
    }

    public void saveSkillConfig() throws IOException {
        this.skillConfig.save(filePath);
    }

    /**
     * Gets the multiplier value for this skill. XP gain will be multiplied by
     * this value inside the SkillzPlayer class (addXP method)
     *
     * @return
     */
    public abstract long getMultiplier();

    /**
     * Checks if the player levels up given the new XP the player has.
     *
     * @param player The player of which is being checked if he levels up.
     * @return true if player levels up, false otherwise.
     */
    public abstract boolean checkLevelup(SkillzPlayer player);

    public abstract void levelUp(SkillzPlayer player, int newLevel);

    public abstract boolean isEnabled();

    public abstract void setEnabled(boolean value);

    public ItemStack[] getRewards(int level) {
        ItemStack[] itemRewards;
        String items = this.getSkillConfig().getString("rewards.items", "");
        if (!items.equals("")) {
            if (items.contains(";")) {
                String[] split = items.split(";");
                itemRewards = new ItemStack[split.length];
                for (int i = 0; i < split.length; i++) {
                    String item = split[i];
                    if (!item.contains(",")) {
                        this.getAPI().getPlugin().getLogger().warning("Unable to load default item '" + item + "' for skill '" + this.getName() + "', no amount set");
                    } else {
                        try {
                            int itemId = Integer.parseInt(item.split(",")[0]);
                            int amount = Integer.parseInt(item.split(",")[1]);
                            byte itemData = item.split(",").length == 3 ? Byte.parseByte(item.split(",")[2]) : 0;
                            itemRewards[i] = new ItemStack(itemId, amount, itemData);
                        } catch (Exception e) {
                            this.getAPI().getPlugin().getLogger().warning("Unable to load default item '" + item + "' for skill '" + this.getName() + "', amount is no number");
                        }
                    }
                }
            } else {
                itemRewards = new ItemStack[1];
                if (!items.contains(",")) {
                    this.getAPI().getPlugin().getLogger().warning("Unable to load default item '" + items + "' for skill '" + this.getName() + "', no amount set");
                } else {
                    try {
                        int itemId = Integer.parseInt(items.split(",")[0]);
                        int amount = Integer.parseInt(items.split(",")[1]);
                        byte itemData = items.split(",").length == 3 ? Byte.parseByte(items.split(",")[2]) : 0;
                        itemRewards[0] = new ItemStack(itemId, amount, itemData);
                    } catch (Exception e) {
                        this.getAPI().getPlugin().getLogger().warning("Unable to load default item '" + items + "' for skill '" + this.getName() + "', amount is no number");
                    }
                }
            }
        } else {
            itemRewards = this.getAPI().getSettings().getDefaultItemRewards();
        }
        return itemRewards;

    }

    public abstract int getMoneyReward();

    public void defaultLevelUp(SkillzPlayer player, int newLevel) {
        Player p = this.getAPI().getPlugin().getServer().getPlayerExact(player.getPlayerName());
        if (this.getSkillConfig().getConfigurationSection("messages.levelup") != null) {
            for (String text : this.getSkillConfig().getStringList("messages.levelup")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        text.replace("%player%", p.getName())
                        .replace("%name%", this.getName())
                        .replace("%newlevel%", "" + newLevel)));
            }
        }
        ItemStack[] itemRewards = this.getRewards(newLevel);
        if (itemRewards != null) {
            HashMap<Integer, ItemStack> back = p.getInventory().addItem(this.getRewards(newLevel));
            if (!back.isEmpty()) {
                for (ItemStack stack : back.values()) {
                    p.getWorld().dropItemNaturally(p.getEyeLocation(), stack);
                }
                p.sendMessage(
                        ChatColor.translateAlternateColorCodes('&',
                                this.getSkillConfig().getString("messages.itemsDroppedOnGround",
                                        "Your inventory was full, causing your item rewards to be dropped on the ground!")));
            }
        }
        if (this.getSkillConfig().getBoolean("broadcastLevelup", this.getAPI().getSettings().isBroadcastLevelup())) {
            for (String text : this.getSkillConfig().getStringList("messages.broadcast")) {
                this.getAPI().getPlugin().getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        text.replace("%player%", p.getName())
                        .replace("%name%", this.getName())
                        .replace("%newlevel%", "" + newLevel)));
            }
        }
    }
}
