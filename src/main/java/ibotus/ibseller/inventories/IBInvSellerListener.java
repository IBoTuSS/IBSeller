package ibotus.ibseller.inventories;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.configurations.IBData;
import ibotus.ibseller.configurations.IBItems;
import ibotus.ibseller.eventmanager.IBEvent;
import ibotus.ibseller.events.IBSellerLevelManager;
import ibotus.ibseller.utils.IBHexColor;
import ibotus.ibseller.utils.IBUtils;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class IBInvSellerListener implements Listener {
    private final Economy econ;
    private final IBInvSeller invSeller;

    public IBInvSellerListener(Economy economy, IBInvSeller invSeller) {
        this.invSeller = invSeller;
        this.econ = economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
            Player player = (Player)event.getWhoClicked();
            if (player.getOpenInventory().getTitle().equals(IBHexColor.color(this.invSeller.getTitle()))) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    int slot = event.getSlot();
                    List<Integer> sellerSlots = IBConfig.getConfig().getIntegerList("inventory.slot-seller-item");
                    if (sellerSlots.contains(slot)) {
                        int itemIndex = sellerSlots.indexOf(slot) + 1;
                        String materialName = IBData.getData().getString("items." + itemIndex + ".material");
                        int price = IBData.getData().getInt("items." + itemIndex + ".price");
                        int amountInConfig = IBItems.getItems().getInt(itemIndex + ".amount");

                        assert materialName != null;

                        if (clickedItem.getType() == Material.getMaterial(materialName)) {
                            int amountInInventory = IBUtils.getAmountInInventory(player, Material.getMaterial(materialName));
                            double multiplier = IBData.getData().getDouble("seller.multiplier");
                            if (event.isLeftClick()) {
                                int count = IBData.getData().getInt("items." + itemIndex + ".count");
                                if (count == 0) {
                                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.seller-count"))));
                                    String soundKey = "sound.items-error";
                                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                    return;
                                }

                                int amountToSell = Math.min(amountInConfig, amountInInventory);
                                if (amountToSell > 0) {
                                    if (count >= amountToSell) {
                                        count -= amountToSell;
                                        IBData.getData().set("items." + itemIndex + ".count", count);
                                        IBData.saveData();
                                    } else {
                                        player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.seller-error"))));
                                        String soundKey = "sound.items-error";
                                        Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                        float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                        float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                        player.playSound(player.getLocation(), sound, volume, pitch);
                                        return;
                                    }

                                    if (amountInInventory < amountInConfig) {
                                        player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.seller-error"))));
                                        String soundKey = "sound.items-error";
                                        Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                        float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                        float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                        player.playSound(player.getLocation(), sound, volume, pitch);
                                        return;
                                    }

                                    IBUtils.removeItemsFromInventory(player, Material.getMaterial(materialName), amountToSell);
                                    double finalPrice = (double)price * multiplier;
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    finalPrice = Double.parseDouble(df.format(finalPrice).replace(',', '.'));
                                    this.econ.depositPlayer(player, finalPrice);
                                    String itemKey = IBUtils.getItemMaterialName(materialName);
                                    String translatedMaterialName = IBUtils.getTranslatedMaterialName(itemKey);
                                    player.sendMessage(IBHexColor.color(Objects.requireNonNull(IBConfig.getConfig().getString("messages.seller-message")).replace("%amount%", String.valueOf(amountToSell)).replace("%material%", (translatedMaterialName)).replace("%price%", String.valueOf(finalPrice))));
                                    String soundKey = "sound.items-sell";
                                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                    int totalSold = IBData.getData().getInt("seller.total", 0);
                                    IBData.getData().set("seller.total", totalSold + amountToSell);
                                    IBData.saveData();
                                    this.invSeller.updateSellerInventory(player.getOpenInventory().getTopInventory());
                                    IBSellerLevelManager.checkAndUpgradeSellerLevel(this.invSeller);
                                    ItemStack item = event.getCurrentItem();
                                    if (IBEvent.isEventRunning() && IBEvent.isEventItem(item)) {
                                        String playerName = player.getName();
                                        int sales = IBEvent.playerSales.getOrDefault(playerName, 0) + amountToSell;
                                        IBEvent.playerSales.put(playerName, sales);
                                        IBEvent.playerTotalSales.put(playerName, IBEvent.playerTotalSales.getOrDefault(playerName, 0) + price * (amountToSell / amountInConfig));
                                    }
                                } else {
                                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.seller-error"))));
                                    String soundKey = "sound.items-error";
                                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                }
                            } else {
                                int count = IBData.getData().getInt("items." + itemIndex + ".count");
                                if (count == 0) {
                                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.seller-count"))));
                                    String soundKey = "sound.items-error";
                                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                    return;
                                }

                                int amountToSell = Math.min(amountInInventory, count);
                                if (amountToSell > 0) {
                                    count -= amountToSell;
                                    IBData.getData().set("items." + itemIndex + ".count", count);
                                    IBData.saveData();
                                    IBUtils.removeItemsFromInventory(player, Material.getMaterial(materialName), amountToSell);
                                    double finalPrice = (double)price * multiplier * ((double)amountToSell / (double)amountInConfig);
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    finalPrice = Double.parseDouble(df.format(finalPrice).replace(',', '.'));
                                    this.econ.depositPlayer(player, finalPrice);
                                    player.sendMessage(IBHexColor.color(Objects.requireNonNull(IBConfig.getConfig().getString("messages.seller-message")).replace("%amount%", String.valueOf(amountToSell)).replace("%material%", (materialName)).replace("%price%", String.valueOf(finalPrice))));
                                    String soundKey = "sound.items-sell";
                                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                    int totalSold = IBData.getData().getInt("seller.total", 0);
                                    IBData.getData().set("seller.total", totalSold + amountToSell);
                                    IBData.saveData();
                                    this.invSeller.updateSellerInventory(player.getOpenInventory().getTopInventory());
                                    IBSellerLevelManager.checkAndUpgradeSellerLevel(this.invSeller);
                                    ItemStack item = event.getCurrentItem();
                                    if (IBEvent.isEventRunning() && IBEvent.isEventItem(item)) {
                                        String playerName = player.getName();
                                        int sales = IBEvent.playerSales.getOrDefault(playerName, 0) + amountToSell;
                                        IBEvent.playerSales.put(playerName, sales);
                                        IBEvent.playerTotalSales.put(playerName, IBEvent.playerTotalSales.getOrDefault(playerName, 0) + price * (amountToSell / amountInConfig));
                                    }
                                } else {
                                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.seller-error"))));
                                    String soundKey = "sound.items-error";
                                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
