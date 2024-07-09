package ibotus.ibseller.utils;

import ibotus.ibseller.IBSeller;
import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.events.SellerEventListener;
import ibotus.ibseller.inventories.InventorySeller;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class EventManager {
    private final IBSeller ibseller;
    private final SellerEventListener sellerEventListener;
    private final InventorySeller invSeller;
    private long lastUpdateTime;

    public EventManager(IBSeller ibseller, InventorySeller invSeller) {
        this.ibseller = ibseller;
        this.invSeller = invSeller;
        this.sellerEventListener = new SellerEventListener(ibseller, invSeller, this);
        resetUpdateTime();
    }

    public void resetUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void startEventTimer() {
        int delay = Config.getConfig().getInt("event.time") * 60 * 20;
        boolean isEventEnabled = Config.getConfig().getBoolean("event.enable");

        if (!isEventEnabled) {
            return;
        }

        new BukkitRunnable(){
            public void run() {
                startEventAndCloseInventory();
            }
        }.runTaskTimer(this.ibseller, delay, delay);
    }

    private void startEventAndCloseInventory() {
        sellerEventListener.startEvent();
        String soundKey = "sound.event-start";
        Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
        float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
        float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTitle().equals(HexColor.color(invSeller.getTitle()))) {
                onlinePlayer.closeInventory();
            }
            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
        }
        List<String> event = Config.getConfig().getStringList("messages.seller-event");
        String translatedMaterialName = Utils.getTranslatedMaterialName(SellerEventListener.randomItemKey);
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            for (String message : event) {
                onlinePlayer.sendMessage(HexColor.color(message.replace("%material%", translatedMaterialName)));
            }
        }
    }


    public String getRemainingTime() {
        int timePassed = (int)((System.currentTimeMillis() - this.lastUpdateTime) / 1000L);
        int updateInterval = Config.getConfig().getInt("event.time") * 60;
        int timeLeft = updateInterval - timePassed;
        return formatTime(timeLeft);
    }

    private String formatTime(int timeLeft) {
        int hours = timeLeft / 3600;
        int minutes = (timeLeft % 3600) / 60;
        int seconds = timeLeft % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}

