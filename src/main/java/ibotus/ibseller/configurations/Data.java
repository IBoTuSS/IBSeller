package ibotus.ibseller.configurations;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Data {
    private static FileConfiguration data;
    private static File file;
    private static final Random random = new Random();

    private static int getRandomPrice(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static void loadYaml(Plugin plugin) {
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            plugin.saveResource("data.yml", true);
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getData() {
        return data;
    }

    public static void saveData() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveItems() {
        data.set("items", null);
        Set<String> itemKeys = new HashSet<>(Items.getItems().getKeys(false));
        List<String> itemKeysList = new ArrayList<>(itemKeys);
        int slots = Config.getConfig().getIntegerList("inventory.slot-seller-item").size();
        Set<String> usedKeys = new HashSet<>();
        for (int i = 0; i < slots; ++i) {
            Collections.shuffle(itemKeysList);
            String key = itemKeysList.get(i);
            while (usedKeys.contains(key)) {
                Collections.shuffle(itemKeysList);
                key = itemKeysList.get(i);
            }
            usedKeys.add(key);
            ConfigurationSection itemSection = Items.getItems().getConfigurationSection(key);
            assert itemSection != null;
            String materialName = itemSection.getString(".material");
            String translatedMaterialName = itemSection.getString(".translated_material");
            int amount = itemSection.getInt(".amount");
            int minPrice = itemSection.getInt(".price.min");
            int maxPrice = itemSection.getInt(".price.max");
            int price = getRandomPrice(minPrice, maxPrice);
            int minCount = itemSection.getInt(".count.min");
            int maxCount = itemSection.getInt(".count.max");
            int count = getRandomPrice(minCount, maxCount);
            String itemPath = "items." + (i + 1);
            data.set(itemPath + ".material", materialName);
            data.set(itemPath + ".translated_material", translatedMaterialName);
            data.set(itemPath + ".amount", amount);
            data.set(itemPath + ".price", price);
            data.set(itemPath + ".count", count);
        }
        setDefaultIfNotSet("seller.total", 0);
        setDefaultIfNotSet("seller.level", 0);
        setDefaultIfNotSet("seller.multiplier", Config.getConfig().getDouble("seller-upgrade.multiplier"));
        saveData();
    }


    private static void setDefaultIfNotSet(String path, Object value) {
        if (!data.isSet(path)) {
            data.set(path, value);
        }
    }
}
