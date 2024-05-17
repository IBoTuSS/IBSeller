package ibotus.ibseller.events;

import ibotus.ibseller.eventmanager.IBEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class IBPlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (IBEvent.isEventRunning() && IBEvent.bossBar != null) {
            IBEvent.bossBar.addPlayer(player);
        }
    }
}
