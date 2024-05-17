package ibotus.ibseller.utils;

import java.util.List;
import java.util.Objects;

import ibotus.ibseller.configurations.IBData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class IBUtils {
    public static ItemStack createItem(String materialName, String name, List<String> lore, Integer customModelData) {
        ItemStack item;
        if (materialName.startsWith("eyJ")) {
            item = IBCustomHead.getSkull(materialName, null);
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
        return IBData.getData().getString("items." + itemKey + ".translated_material");
    }

    public static String getItemMaterialName(String materialName) {
        for (String key : Objects.requireNonNull(IBData.getData().getConfigurationSection("items")).getKeys(false)) {
            String currentMaterialName = IBData.getData().getString("items." + key + ".material");
            if (currentMaterialName != null && currentMaterialName.equalsIgnoreCase(materialName)) {
                return key;
            }
        }
        return null;
    }
}


