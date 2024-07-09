package ibotus.ibseller.configurations;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class Items {
    private static FileConfiguration items;

    public static void loadYaml(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "items.yml");
        if (!file.exists()) {
            plugin.saveResource("items.yml", true);
        }
        items = YamlConfiguration.loadConfiguration(file);

        for (String key : items.getKeys(false)) {
            String materialName = items.getString(key + ".material");
            assert materialName != null;
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Материал " + materialName + " не существует в элементе " + key);
            }

            int amount = items.getInt(key + ".amount");
            if (amount < 1 || amount > 64) {
                plugin.getLogger().warning("Количество " + amount + " в элементе " + key + " не в диапазоне от 1 до 64");
            }

            double minPrice = items.getDouble(key + ".price.min");
            double maxPrice = items.getDouble(key + ".price.max");
            if (minPrice < 1 || maxPrice < minPrice) {
                plugin.getLogger().warning("Цена в элементе " + key + " не соответствует требованиям!");
            }

            int minCount = items.getInt(key + ".count.min");
            int maxCount = items.getInt(key + ".count.max");
            if (minCount < 1 || maxCount < minCount) {
                plugin.getLogger().warning("Количество в элементе " + key + " не соответствует требованиям!");
            }
        }
    }

    public static FileConfiguration getItems() {
        return items;
    }
}
