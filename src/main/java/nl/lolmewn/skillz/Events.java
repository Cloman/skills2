package nl.lolmewn.skillz;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Sybren
 */
public class Events implements Listener {
    
    private Main m;
    
    public Events(Main m){
        this.m = m;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        m.getPlayerManager().loadPlayer(event.getPlayer().getName());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        m.getPlayerManager().savePlayer(event.getPlayer().getName(), true);
    }

}
