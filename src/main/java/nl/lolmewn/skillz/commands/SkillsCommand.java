package nl.lolmewn.skillz.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import nl.lolmewn.skillz.Main;
import nl.lolmewn.skillz.api.Skill;
import nl.lolmewn.skillz.players.SkillzPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Lolmewn
 */
public class SkillsCommand implements CommandExecutor {

    private Main plugin;

    public SkillsCommand(Main m) {
        this.plugin = m;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command is for players only.");
                return true;
            }
            SkillzPlayer player = plugin.getPlayerManager().getPlayer(sender.getName());
            sendSkills(sender, player, 1);
            return true;
        }
        return true;
    }

    public void sendSkills(CommandSender sender, SkillzPlayer player, int page) {
        ArrayList<String> skills = new ArrayList<String>();
        int sent = 0, totalLevel = 0;
        double totalXP = 0;
        for (String skillName : player.getSkills()) {
            if (sent >= 8) {
                break;
            }
            Skill skill = this.plugin.getSkillManager().get(skillName);
            if (skill == null) {
                continue;
            }
            if (!skill.isEnabled()) {
                continue;
            }
            skills.add(skillName);
            totalLevel += player.getLevel(skillName);
            totalXP += player.getXP(skillName);
        }
        if (!(skills.size() > page * 8 - 8)) {
            sender.sendMessage(ChatColor.RED + "There is no page " + page + " for " + player.getPlayerName() + "!");
            return;
        }
        int getCounter = (page - 1) * 8;
        while (sent != 8 && getCounter < skills.size()) {
            String skill = skills.get(getCounter++);
            double remaining = player.getLevel(skill) * player.getLevel(skill) * 10 - player.getXP(skill);
            double percent = 100 - (remaining / (player.getLevel(skill) * player.getLevel(skill) * 10 - (player.getLevel(skill) - 1) * (player.getLevel(skill) - 1) * 10) * 100);
            int stripes = (int) percent / (100 / 20); //Draws the red stripes
            if (player.getLevel(skill) == 0) {
                stripes = 0;
            }
            StringBuilder str = new StringBuilder();
            str.append(ChatColor.WHITE).append("[");
            for (int b = 0; b < stripes; b++) {
                str.append(ChatColor.GREEN).append("|");
            }
            for (int a = 0; a < 20 - stripes; a++) {
                str.append(ChatColor.RED).append("|");
            }
            str.append(ChatColor.WHITE).append("]");
            sender.sendMessage(ChatColor.RED + skill + ChatColor.WHITE + " Level: " + ChatColor.GREEN + player.getLevel(skill) + ChatColor.WHITE + " XP: " + ChatColor.GREEN + new DecimalFormat("0.#").format(player.getXP(skill)) + " " + str.toString());
            sent++;
        }
    }
}
