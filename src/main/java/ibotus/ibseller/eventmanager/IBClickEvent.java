package ibotus.ibseller.eventmanager;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.utils.IBHexColor;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class IBClickEvent implements Listener {

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        String title = IBConfig.getConfig().getString("inventory.title");
        if (title != null && event.getView().getTitle().equals(IBHexColor.color(title))) {
            event.setCancelled(true);

            int clickedSlot = event.getRawSlot();
            if (clickedSlot >= 0 && clickedSlot < event.getInventory().getSize()) {
                for (String slotStr : Objects.requireNonNull(IBConfig.getConfig().getConfigurationSection("inventory.slots-item")).getKeys(false)) {
                    int slot = IBConfig.getConfig().getInt("inventory.slots-item." + slotStr + ".slot");
                    if (clickedSlot == slot) {
                        List<String> commands = IBConfig.getConfig().getStringList("inventory.slots-item." + slotStr + ".commands");
                        Player player = (Player) event.getWhoClicked();
                        for (String cmd : commands) {
                            if (cmd.startsWith("[cmd]")) {
                                String playerCmd = cmd.replace("[cmd]", "").trim();
                                Bukkit.dispatchCommand(player, playerCmd);
                            } else if (cmd.equals("[close]")) {
                                player.closeInventory();
                            } else if (cmd.startsWith("[sound]")) {
                                String soundName = cmd.replace("[sound]", "").trim();
                                try {
                                    Sound sound = Sound.valueOf(soundName);
                                    player.playSound(player.getLocation(), sound, 1F, 1F);
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
}
