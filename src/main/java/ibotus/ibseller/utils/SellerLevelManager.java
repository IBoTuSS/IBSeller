package ibotus.ibseller.utils;

import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.inventories.InventorySeller;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SellerLevelManager {
    public static void checkAndUpgradeSellerLevel(InventorySeller invSeller) {
        int totalSold = Data.getData().getInt("seller.total", 0);
        int sellerLevel = Data.getData().getInt("seller.level", 0);
        int maxLevel = Config.getConfig().getInt("seller-upgrade.levels");
        double multiplier = Data.getData().getDouble("seller.multiplier");
        double multiplierUpgrade = Config.getConfig().getDouble("seller-upgrade.multiplierupgrade");
        if (sellerLevel < maxLevel && totalSold >= Config.getConfig().getInt("seller-upgrade.amount-upgrade." + (sellerLevel + 1))) {
            multiplier += multiplierUpgrade;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
            multiplier = Double.parseDouble(df.format(multiplier));
            Data.getData().set("seller.level", ++sellerLevel);
            Data.getData().set("seller.multiplier", multiplier);
            Data.getData().set("seller.total", 0);
            Data.saveData();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.getOpenInventory().getTitle().equals(HexColor.color(invSeller.getTitle()))) continue;
                onlinePlayer.closeInventory();
            }
            String levelSymbol = Config.getConfig().getString("seller-upgrade.replace-levels." + (sellerLevel <= maxLevel ? Integer.valueOf(sellerLevel) : "maximum"));
            List<String> messages = sellerLevel < maxLevel ? Config.getConfig().getStringList("seller-upgrade.messages.upgrade-level") : Collections.singletonList(Config.getConfig().getString("seller-upgrade.messages.maximum-level"));
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                for (String message : messages) {
                    assert (levelSymbol != null);
                    onlinePlayer.sendMessage(HexColor.color(message.replace("%levels%", levelSymbol)));
                }
            }
            String soundKey = sellerLevel < maxLevel ? "sound.upgrade-level" : "sound.maximum-level";
            Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
            float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
            float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
            }
        }
    }
}

