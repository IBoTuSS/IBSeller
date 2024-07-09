package ibotus.ibseller.utils;

import java.util.List;

import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.inventories.InventorySeller;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SellerUpdater {

    private final JavaPlugin plugin;
    private long lastUpdateTime;

    public SellerUpdater(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void resetUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void start(InventorySeller invSeller) {
        int updateInterval = Config.getConfig().getInt("settings.update") * 60 * 20;
        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            resetUpdateTime();
            Data.saveItems();
            broadcastUpdateMessages();
            closeInventoryAndPlaySound(invSeller);
        }, 0L, updateInterval);
    }

    private void broadcastUpdateMessages() {
        List<String> updateMessages = Config.getConfig().getStringList("messages.seller-update");
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            for (String message : updateMessages) {
                onlinePlayer.sendMessage(HexColor.color(message));
            }
        }
    }


    private void closeInventoryAndPlaySound(InventorySeller invSeller) {
        String soundKey = "sound.seller-update";
        Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
        float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
        float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTitle().equals(HexColor.color(invSeller.getTitle()))) {
                onlinePlayer.closeInventory();
            }
            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
        }
    }

    public String getRemainingTime() {
        int timePassed = (int)((System.currentTimeMillis() - this.lastUpdateTime) / 1000L);
        int updateInterval = Config.getConfig().getInt("settings.update") * 60;
        int timeLeft = updateInterval - timePassed;
        return formatTime(timeLeft);
    }

    private String formatTime(int timeLeft) {
        int hours = timeLeft / 3600;
        int minutes = (timeLeft % 3600) / 60;
        int seconds = timeLeft % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public String replacePlaceholder(String loreLine) {
        if (loreLine.contains("%update%")) {
            loreLine = loreLine.replace("%update%", getRemainingTime());
        }
        return loreLine;
    }
}
