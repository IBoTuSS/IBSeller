package ibotus.ibseller.utils;

import java.util.List;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.configurations.IBData;
import ibotus.ibseller.inventories.IBInvSeller;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class IBSellerUpdater {

    private final JavaPlugin plugin;
    private long lastUpdateTime;

    public IBSellerUpdater(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void resetUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void start(IBInvSeller invSeller) {
        int updateInterval = IBConfig.getConfig().getInt("settings.update") * 60 * 20;
        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            resetUpdateTime();
            IBData.saveItems();
            broadcastUpdateMessages();
            closeInventoryAndPlaySound(invSeller);
        }, 0L, updateInterval);
    }

    private void broadcastUpdateMessages() {
        List<String> updateMessages = IBConfig.getConfig().getStringList("messages.seller-update");
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            for (String message : updateMessages) {
                onlinePlayer.sendMessage(IBHexColor.color(message));
            }
        }
    }


    private void closeInventoryAndPlaySound(IBInvSeller invSeller) {
        String soundKey = "sound.seller-update";
        Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
        float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
        float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTitle().equals(IBHexColor.color(invSeller.getTitle()))) {
                onlinePlayer.closeInventory();
            }
            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
        }
    }

    public String getRemainingTime() {
        int timePassed = (int)((System.currentTimeMillis() - this.lastUpdateTime) / 1000L);
        int updateInterval = IBConfig.getConfig().getInt("settings.update") * 60;
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
