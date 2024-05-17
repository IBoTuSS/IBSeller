package ibotus.ibseller.events;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.configurations.IBData;
import ibotus.ibseller.inventories.IBInvSeller;
import ibotus.ibseller.utils.IBHexColor;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class IBSellerLevelManager {
    public static void checkAndUpgradeSellerLevel(IBInvSeller invSeller) {
        int totalSold = IBData.getData().getInt("seller.total", 0);
        int sellerLevel = IBData.getData().getInt("seller.level", 0);
        int maxLevel = IBConfig.getConfig().getInt("seller-upgrade.levels");
        double multiplier = IBData.getData().getDouble("seller.multiplier");
        double multiplierUpgrade = IBConfig.getConfig().getDouble("seller-upgrade.multiplierupgrade");
        if (sellerLevel < maxLevel && totalSold >= IBConfig.getConfig().getInt("seller-upgrade.amount-upgrade." + (sellerLevel + 1))) {
            multiplier += multiplierUpgrade;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
            multiplier = Double.parseDouble(df.format(multiplier));
            IBData.getData().set("seller.level", ++sellerLevel);
            IBData.getData().set("seller.multiplier", multiplier);
            IBData.getData().set("seller.total", 0);
            IBData.saveData();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.getOpenInventory().getTitle().equals(IBHexColor.color(invSeller.getTitle()))) continue;
                onlinePlayer.closeInventory();
            }
            String levelSymbol = IBConfig.getConfig().getString("seller-upgrade.replace-levels." + (sellerLevel <= maxLevel ? Integer.valueOf(sellerLevel) : "maximum"));
            List<String> messages = sellerLevel < maxLevel ? IBConfig.getConfig().getStringList("seller-upgrade.messages.upgrade-level") : Collections.singletonList(IBConfig.getConfig().getString("seller-upgrade.messages.maximum-level"));
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                for (String message : messages) {
                    assert (levelSymbol != null);
                    onlinePlayer.sendMessage(IBHexColor.color(message.replace("%levels%", levelSymbol)));
                }
            }
            String soundKey = sellerLevel < maxLevel ? "sound.upgrade-level" : "sound.maximum-level";
            Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
            float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
            float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
            }
        }
    }
}

