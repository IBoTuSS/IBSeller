package ibotus.ibseller.events;

import ibotus.ibseller.inventories.InventorySeller;
import ibotus.ibseller.utils.HexColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {
    private final InventorySeller invSeller;

    public InventoryCloseListener(InventorySeller invSeller) {
        this.invSeller = invSeller;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getView().getTitle().equals(HexColor.color(invSeller.getTitle()))) {
            Integer taskId = InventorySeller.tasks.get(player.getUniqueId());
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
                InventorySeller.tasks.remove(player.getUniqueId());
            }
        }
    }
}

