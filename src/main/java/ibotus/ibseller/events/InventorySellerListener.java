package ibotus.ibseller.events;

import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.configurations.Items;
import ibotus.ibseller.inventories.InventorySeller;
import ibotus.ibseller.utils.HexColor;
import ibotus.ibseller.utils.Utils;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InventorySellerListener implements Listener {
    private final Economy econ;
    private final InventorySeller invSeller;

    public InventorySellerListener(Economy economy, InventorySeller invSeller) {
        this.invSeller = invSeller;
        this.econ = economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
            Player player = (Player)event.getWhoClicked();
            if (player.getOpenInventory().getTitle().equals(HexColor.color(this.invSeller.getTitle()))) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    int slot = event.getSlot();
                    List<Integer> sellerSlots = Config.getConfig().getIntegerList("inventory.slot-seller-item");
                    if (sellerSlots.contains(slot)) {
                        int itemIndex = sellerSlots.indexOf(slot) + 1;
                        String materialName = Data.getData().getString("items." + itemIndex + ".material");
                        int price = Data.getData().getInt("items." + itemIndex + ".price");
                        int amountInConfig = Items.getItems().getInt(itemIndex + ".amount");

                        assert materialName != null;

                        if (clickedItem.getType() == Material.getMaterial(materialName)) {
                            int amountInInventory = Utils.getAmountInInventory(player, Material.getMaterial(materialName));
                            double multiplier = Data.getData().getDouble("seller.multiplier");
                            if (event.isLeftClick()) {
                                int count = Data.getData().getInt("items." + itemIndex + ".count");
                                if (count == 0) {
                                    sendErrorMessage(player, "messages.seller-count");
                                    return;
                                }

                                int amountToSell = Math.min(amountInConfig, amountInInventory);
                                if (amountToSell > 0) {
                                    if (count >= amountToSell) {
                                        count -= amountToSell;
                                        Data.getData().set("items." + itemIndex + ".count", count);
                                        Data.saveData();
                                    } else {
                                        sendErrorMessage(player, "messages.seller-error");
                                        return;
                                    }

                                    if (amountInInventory < amountInConfig) {
                                        sendErrorMessage(player, "messages.seller-error");
                                        return;
                                    }

                                    Utils.removeItemsFromInventory(player, Material.getMaterial(materialName), amountToSell);
                                    double finalPrice = calculateFinalPrice(price, multiplier);
                                    econ.depositPlayer(player, finalPrice);
                                    sendMessage(player, amountToSell, materialName, finalPrice);
                                    playSound(player, "sound.items-sell");

                                    updateSellerData(amountToSell);
                                    invSeller.updateSellerInventory(player.getOpenInventory().getTopInventory());
                                    checkAndUpgradeSellerLevel(invSeller);
                                    updateEventSales(player, clickedItem, amountToSell, price, amountInConfig);
                                } else {
                                    sendErrorMessage(player, "messages.seller-error");
                                }
                            } else {
                                int count = Data.getData().getInt("items." + itemIndex + ".count");
                                if (count == 0) {
                                    sendErrorMessage(player, "messages.seller-count");
                                    return;
                                }

                                int amountToSell = Math.min(amountInInventory, count);
                                if (amountToSell > 0) {
                                    count -= amountToSell;
                                    Data.getData().set("items." + itemIndex + ".count", count);
                                    Data.saveData();
                                    Utils.removeItemsFromInventory(player, Material.getMaterial(materialName), amountToSell);
                                    double finalPrice = calculateFinalPrice(price * ((double)amountToSell / (double)amountInConfig), multiplier);
                                    econ.depositPlayer(player, finalPrice);
                                    sendMessage(player, amountToSell, materialName, finalPrice);
                                    playSound(player, "sound.items-sell");

                                    updateSellerData(amountToSell);
                                    invSeller.updateSellerInventory(player.getOpenInventory().getTopInventory());
                                    checkAndUpgradeSellerLevel(invSeller);
                                    updateEventSales(player, clickedItem, amountToSell, price, amountInConfig);
                                } else {
                                    sendErrorMessage(player, "messages.seller-error");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendErrorMessage(Player player, String messageKey) {
        player.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString(messageKey))));
        playSound(player, "sound.items-error");
    }

    private void sendMessage(Player player, int amountToSell, String materialName, double finalPrice) {
        player.sendMessage(HexColor.color(Objects.requireNonNull(Config.getConfig().getString("messages.seller-message"))
                .replace("%amount%", String.valueOf(amountToSell))
                .replace("%material%", Objects.requireNonNull(Utils.getItemMaterialName(materialName)))
                .replace("%price%", String.valueOf(finalPrice))));
    }

    private void playSound(Player player, String soundKey) {
        Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
        float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
        float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private double calculateFinalPrice(double price, double multiplier) {
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(price * multiplier).replace(',', '.'));
    }

    private void updateSellerData(int amountToSell) {
        int totalSold = Data.getData().getInt("seller.total", 0);
        Data.getData().set("seller.total", totalSold + amountToSell);
        Data.saveData();
    }

    private void updateEventSales(Player player, ItemStack item, int amountToSell, int price, int amountInConfig) {
        if (SellerEventListener.isEventRunning() && SellerEventListener.isEventItem(item)) {
            String playerName = player.getName();
            int sales = SellerEventListener.playerSales.getOrDefault(playerName, 0) + amountToSell;
            SellerEventListener.playerSales.put(playerName, sales);
            int totalSales = SellerEventListener.playerSales.getOrDefault(playerName, 0) + price * (amountToSell / amountInConfig);
            SellerEventListener.playerSales.put(playerName, totalSales);
        }
    }

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
