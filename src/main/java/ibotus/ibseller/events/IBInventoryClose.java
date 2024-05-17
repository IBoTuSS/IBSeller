package ibotus.ibseller.events;

import ibotus.ibseller.inventories.IBInvSeller;
import ibotus.ibseller.utils.IBHexColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class IBInventoryClose implements Listener {
    private final IBInvSeller invSeller;

    public IBInventoryClose (IBInvSeller invSeller) {
        this.invSeller = invSeller;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getView().getTitle().equals(IBHexColor.color(invSeller.getTitle()))) {
            Integer taskId = IBInvSeller.tasks.get(player.getUniqueId());
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
                IBInvSeller.tasks.remove(player.getUniqueId());
            }
        }
    }
}

