package ibotus.ibseller.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (SellerEventListener.isEventRunning() && SellerEventListener.bossBar != null) {
            SellerEventListener.bossBar.addPlayer(player);
        }
    }
}
