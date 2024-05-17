package ibotus.ibseller.utils;

import ibotus.ibseller.IBSeller;
import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.eventmanager.IBEvent;
import ibotus.ibseller.inventories.IBInvSeller;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class IBEventManager {
    private final IBSeller ibseller;
    private final IBEvent ibEvent;
    private final IBInvSeller invSeller;
    private long lastUpdateTime;

    public IBEventManager(IBSeller ibseller, IBInvSeller invSeller) {
        this.ibseller = ibseller;
        this.invSeller = invSeller;
        this.ibEvent = new IBEvent(ibseller, invSeller, this);
        resetUpdateTime();
    }

    public void resetUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void startEventTimer() {
        int delay = IBConfig.getConfig().getInt("event.time") * 60 * 20;
        boolean isEventEnabled = IBConfig.getConfig().getBoolean("event.enable");

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
        ibEvent.startEvent();
        String soundKey = "sound.event-start";
        Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
        float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
        float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTitle().equals(IBHexColor.color(invSeller.getTitle()))) {
                onlinePlayer.closeInventory();
            }
            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
        }
        List<String> event = IBConfig.getConfig().getStringList("messages.seller-event");
        String translatedMaterialName = IBUtils.getTranslatedMaterialName(IBEvent.randomItemKey);
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            for (String message : event) {
                onlinePlayer.sendMessage(IBHexColor.color(message.replace("%material%", translatedMaterialName)));
            }
        }
    }


    public String getRemainingTime() {
        int timePassed = (int)((System.currentTimeMillis() - this.lastUpdateTime) / 1000L);
        int updateInterval = IBConfig.getConfig().getInt("event.time") * 60;
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

