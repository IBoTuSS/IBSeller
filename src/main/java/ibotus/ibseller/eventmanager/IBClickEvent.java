package ibotus.ibseller.eventmanager;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.utils.IBHexColor;
import ibotus.ibseller.utils.IBUtils;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class IBClickEvent implements Listener {

    private final Economy econ;
    private final PlayerPoints playerPoints;
    private final IBUtils ibUtils;

    public IBClickEvent(Economy economy, PlayerPoints playerpoints, IBUtils ibUtils) {
        this.ibUtils = ibUtils;
        this.econ = economy;
        this.playerPoints = playerpoints;
    }

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
                            try {
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
                                    } catch (IllegalArgumentException e) {
                                        Bukkit.getLogger().warning("Неверное название звука: " + soundName);
                                    }
                                } else if (cmd.startsWith("[update]")) {
                                    if (IBEvent.isEventRunning()) {
                                        player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.event-running"))));
                                    }
                                    String[] parts = cmd.split(" ");
                                    if (parts.length >= 2) {
                                        try {
                                            double cost = Double.parseDouble(parts[1]);
                                            String economyType = IBConfig.getConfig().getString("settings.update-economy", "Vault");
                                            if (economyType.equalsIgnoreCase("Vault")) {
                                                if (econ.has(player, cost)) {
                                                    econ.withdrawPlayer(player, cost);
                                                    updateSeller(player);
                                                } else {
                                                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.no-money"))));
                                                }
                                            } else if (economyType.equalsIgnoreCase("PlayerPoints")) {
                                                if (playerPoints.getAPI().take(player.getUniqueId(), (int) cost)) {
                                                    updateSeller(player);
                                                } else {
                                                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.no-money"))));
                                                }
                                            } else {
                                                Bukkit.getLogger().warning("Неподдерживаемый тип экономики: " + economyType);
                                            }
                                        } catch (NumberFormatException e) {
                                            Bukkit.getLogger().warning("Неверный формат команды обновления: " + cmd);
                                        }
                                    } else {
                                        Bukkit.getLogger().warning("В команде обновления отсутствуют аргументы: " + cmd);
                                    }
                                } else {
                                    Bukkit.getLogger().warning("Неподдерживаемый формат команды: " + cmd);
                                }
                            } catch (Exception e) {
                                Bukkit.getLogger().severe("Ошибка выполнения команды: " + cmd);
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateSeller(Player player) {
        this.ibUtils.getRegeneratedItem();
        List<String> updateMessages = IBConfig.getConfig().getStringList("messages.player-update");
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            for (String message : updateMessages) {
                onlinePlayer.sendMessage(IBHexColor.color(message.replace("%player%", player.getName())));
            }
        }
    }
}
