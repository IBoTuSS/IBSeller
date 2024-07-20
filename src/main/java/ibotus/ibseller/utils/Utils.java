package ibotus.ibseller.utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.inventories.InventorySeller;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {

    private final SellerUpdater SellerUpdater;
    private final InventorySeller invSeller;

    public Utils(SellerUpdater SellerUpdater, InventorySeller invSeller) {
        this.SellerUpdater = SellerUpdater;
        this.invSeller = invSeller;
    }

    public static ItemStack createItem(String materialName, String name, List<String> lore, Integer customModelData) {
        ItemStack item;
        if (materialName.startsWith("eyJ")) {
            item = getSkull(materialName, null);
        } else {
            Material material = Material.getMaterial(materialName);
            assert (material != null);
            item = new ItemStack(material);
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        if (customModelData != null && customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(String materialName, int amount, List<String> lore, Integer customModelData) {
        Material material = Material.getMaterial(materialName);
        assert (material != null);
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        if (customModelData != null && customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static int getAmountInInventory(Player player, Material material) {
        int count = 0;

        for (int j = 0; j < player.getInventory().getStorageContents().length; ++j) {
            ItemStack sl = player.getInventory().getItem(j);
            if (sl != null && sl.isSimilar(new ItemStack(material))) {
                count += sl.getAmount();
            }
        }

        return count;
    }

    public static void removeItemsFromInventory(Player player, Material material, int amountToRemove) {
        for (int j = 0; j < player.getInventory().getContents().length; ++j) {
            ItemStack sl = player.getInventory().getItem(j);
            if (sl != null && material == sl.getType()) {
                int slotamount = sl.getAmount();
                if (slotamount != 0) {
                    if (slotamount <= amountToRemove) {
                        amountToRemove -= slotamount;
                        player.getInventory().setItem(j, null);
                    } else {
                        sl.setAmount(slotamount - amountToRemove);
                        amountToRemove = 0;
                    }

                    if (amountToRemove == 0) {
                        break;
                    }
                }
            }
        }
        player.updateInventory();
    }

    public static String getTranslatedMaterialName(String itemKey) {
        return Data.getData().getString("items." + itemKey + ".translated_material");
    }

    public static String getItemMaterialName(String materialName) {
        for (String key : Objects.requireNonNull(Data.getData().getConfigurationSection("items")).getKeys(false)) {
            String currentMaterialName = Data.getData().getString("items." + key + ".material");
            if (currentMaterialName != null && currentMaterialName.equalsIgnoreCase(materialName)) {
                return key;
            }
        }
        return null;
    }

    public void getRegeneratedItem() {
        SellerUpdater.resetUpdateTime();
        Data.saveItems();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTitle().equals(HexColor.color(invSeller.getTitle()))) {
                onlinePlayer.closeInventory();
            }
            String soundKey = "sound.player-update";
            Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
            float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
            float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");
            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
        }
    }

    public static ItemStack getSkull(String url, String path) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        if (Bukkit.getBukkitVersion().contains("1.12")) {
            item = new ItemStack(Material.valueOf("HEAD"));
        }
        ItemMeta meta = item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        try {
            assert (meta != null);
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        }
        catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException var6) {
            Bukkit.getLogger().severe("Такой головы не существует: " + path);
        }
        item.setItemMeta(meta);
        return item;
    }
}


