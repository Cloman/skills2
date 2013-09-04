package nl.lolmewn.skillz.commands;

import nl.lolmewn.skillz.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Lolmewn
 */
public class SkillsCommand implements CommandExecutor{

    private Main plugin;
    
    public SkillsCommand(Main m){
        this.plugin = m;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }

}
