package ibotus.ibseller.inventories;

import ibotus.ibseller.IBSeller;
import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.events.SellerEventListener;
import ibotus.ibseller.utils.HexColor;
import ibotus.ibseller.utils.SellerUpdater;
import ibotus.ibseller.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventorySeller implements Listener {
    private final SellerUpdater SellerUpdater;
    private final IBSeller plugin;
    private String title;
    public static final HashMap<UUID, Integer> tasks = new HashMap<>();

    public InventorySeller(SellerUpdater SellerUpdater, IBSeller plugin) {
        this.SellerUpdater = SellerUpdater;
        this.plugin = plugin;
    }

    public String getTitle() {
        return this.title;
    }

    public Inventory InvSeller() {
        this.title = HexColor.color(Config.getConfig().getString("inventory.title"));
        int size = Config.getConfig().getInt("inventory.size");
        Inventory inv = Bukkit.createInventory(null, size, this.title);
        List<Integer> sellerSlots = Config.getConfig().getIntegerList("inventory.slot-seller-item");
        for (int i = 0; i < sellerSlots.size(); ++i) {
            int slot = sellerSlots.get(i);
            String materialName = Data.getData().getString("items." + (i + 1) + ".material");
            int amount = Data.getData().getInt("items." + (i + 1) + ".amount");
            int price = Data.getData().getInt("items." + (i + 1) + ".price");
            int count = Data.getData().getInt("items." + (i + 1) + ".count");
            double multiplier = Data.getData().getDouble("seller.multiplier");
            Integer customModelData = Data.getData().getInt("items." + (i + 1) + ".custom-model-data");
            List<String> sellLore = this.colorizeLore(Config.getConfig().getStringList("inventory.lore-sell-item"));
            sellLore = sellLore.stream().map(line -> line.replace("%multiplier%", String.valueOf(multiplier))).map(line -> line.replace("%price%", String.valueOf(price))).map(line -> line.replace("%count%", String.valueOf(count))).map(line -> line.replace("%amount%", String.valueOf(amount))).collect(Collectors.toList());
            ItemStack item = Utils.createItem(materialName, amount, sellLore, customModelData);

            if (SellerEventListener.isEventRunning() && Config.getConfig().getBoolean("event.enchant") && SellerEventListener.isEventItem(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
            }
            inv.setItem(slot, item);
        }

        for (String key : Objects.requireNonNull(Config.getConfig().getConfigurationSection("inventory.slots-item")).getKeys(false)) {
            int slot = Config.getConfig().getInt("inventory.slots-item." + key + ".slot");
            String materialName = Config.getConfig().getString("inventory.slots-item." + key + ".material");
            String name = HexColor.color(Config.getConfig().getString("inventory.slots-item." + key + ".name"));
            Integer customModelData = Config.getConfig().getInt("inventory.slots-item." + key + ".custom-model-data");
            List<String> lore = this.colorizeLore(Config.getConfig().getStringList("inventory.slots-item." + key + ".lore"));
            assert (materialName != null);
            ItemStack item = Utils.createItem(materialName, name, lore, customModelData);
            inv.setItem(slot, item);
        }


        return inv;
    }

    public void openInventory(Player p, InventorySeller invSeller) {
        Inventory inv = invSeller.InvSeller();
        p.openInventory(inv);

        if (!tasks.containsKey(p.getUniqueId())) {
            tasks.put(p.getUniqueId(), Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (tasks.containsKey(p.getUniqueId())) {
                    try {
                        invSeller.updateSellerInventory(inv);
                    } catch (NullPointerException e) {
                        openInventory(p, invSeller);
                    }
                }
            }, 0L, 20L).getTaskId());
        }
    }

    public void updateSellerInventory(Inventory inventory) {
        List<Integer> sellerSlots = Config.getConfig().getIntegerList("inventory.slot-seller-item");
        int totalSold = Data.getData().getInt("seller.total", 0);
        for (int i = 0; i < sellerSlots.size(); ++i) {
            int slot = sellerSlots.get(i);
            String materialName = Data.getData().getString("items." + (i + 1) + ".material");
            int amount = Data.getData().getInt("items." + (i + 1) + ".amount");
            int price = Data.getData().getInt("items." + (i + 1) + ".price");
            int count = Data.getData().getInt("items." + (i + 1) + ".count");
            double multiplier = Data.getData().getDouble("seller.multiplier");
            Integer customModelData = Data.getData().getInt("items." + (i + 1) + ".custom-model-data");
            List<String> sellLore = this.colorizeLore(Config.getConfig().getStringList("inventory.lore-sell-item"));
            sellLore = sellLore.stream().map(line -> line.replace("%multiplier%", String.valueOf(multiplier))).map(line -> line.replace("%price%", String.valueOf(price))).map(line -> line.replace("%count%", String.valueOf(count))).map(line -> line.replace("%amount%", String.valueOf(amount))).map(line -> line.replace("%total_sold%", String.valueOf(totalSold))).collect(Collectors.toList());
            ItemStack item = Utils.createItem(materialName, amount, sellLore, customModelData);
            if (SellerEventListener.isEventRunning() && Config.getConfig().getBoolean("event.enchant") && SellerEventListener.isEventItem(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
            }
            inventory.setItem(slot, item);
        }

        for (String key : Objects.requireNonNull(Config.getConfig().getConfigurationSection("inventory.slots-item")).getKeys(false)) {
            int slot = Config.getConfig().getInt("inventory.slots-item." + key + ".slot");
            String materialName = Config.getConfig().getString("inventory.slots-item." + key + ".material");
            String name = HexColor.color(Config.getConfig().getString("inventory.slots-item." + key + ".name"));
            Integer customModelData = Config.getConfig().getInt("inventory.slots-item." + key + ".custom-model-data");
            List<String> lore = this.colorizeLore(Config.getConfig().getStringList("inventory.slots-item." + key + ".lore"));
            lore = lore.stream().map(line -> line.replace("%total_sold%", String.valueOf(totalSold))).collect(Collectors.toList());
            assert (materialName != null);
            ItemStack item = Utils.createItem(materialName, name, lore, customModelData);
            inventory.setItem(slot, item);
        }
    }

    private List<String> colorizeLore(List<String> lore) {
        int totalSold = Data.getData().getInt("seller.total", 0);
        int sellerLevel = Data.getData().getInt("seller.level", 1);
        int maxLevel = Config.getConfig().getInt("seller-upgrade.levels");
        String levelSymbol = Config.getConfig().getString("seller-upgrade.replace-levels." + (sellerLevel < maxLevel ? Integer.valueOf(sellerLevel) : "maximum"));
        String amountUpgrade = sellerLevel < maxLevel ? String.valueOf(Config.getConfig().getInt("seller-upgrade.amount-upgrade." + (sellerLevel + 1))) : Config.getConfig().getString("seller-upgrade.amount-upgrade.maximum");
        return lore.stream().map(line -> {
            if (line.contains("%update%")) {
                line = this.SellerUpdater.replacePlaceholder(line);
            }
            if (line.contains("%total_sold%")) {
                line = line.replace("%total_sold%", sellerLevel < maxLevel ? totalSold + "/" + amountUpgrade : Objects.requireNonNull(amountUpgrade));
            }
            if (line.contains("%levels%")) {
                assert (levelSymbol != null);
                line = line.replace("%levels%", levelSymbol);
            }
            return HexColor.color(line);
        }).collect(Collectors.toList());
    }
}
