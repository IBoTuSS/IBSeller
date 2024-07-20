package ibotus.ibseller.events;

import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.inventories.InventorySeller;
import ibotus.ibseller.utils.EventManager;
import ibotus.ibseller.utils.HexColor;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;

public class SellerEventListener {
    private final JavaPlugin plugin;
    private final InventorySeller invSeller;
    private final HashMap<String, Integer> oldPrices = new HashMap<>();
    public static final HashMap<String, Integer> playerSales = new HashMap<>();
    public static boolean isEventRunning = false;
    public static String randomItemKey;
    public static BossBar bossBar;
    private final EventManager eventManager;

    public static boolean isEventRunning() {
        return isEventRunning;
    }

    public SellerEventListener(JavaPlugin plugin, InventorySeller invSeller, EventManager eventManager) {
        this.plugin = plugin;
        this.invSeller = invSeller;
        this.eventManager = eventManager;
    }

    public static boolean isEventItem(ItemStack item) {
        String currentItemMaterialName = item.getType().name();
        String eventItemMaterialName = Data.getData().getString("items." + randomItemKey + ".material");
        return currentItemMaterialName.equals(eventItemMaterialName);
    }

    public boolean isItemAvailableInSeller(String itemMaterialName) {
        for (String key : Objects.requireNonNull(Data.getData().getConfigurationSection("items")).getKeys(false)) {
            String materialName = Data.getData().getString("items." + key + ".material");
            if (materialName != null && materialName.equalsIgnoreCase(itemMaterialName)) {
                return true;
            }
        }
        return false;
    }

    public void startEvent() {
        if (isEventRunning) {
            return;
        }

        List<String> itemKeys = new ArrayList<>(Objects.requireNonNull(Data.getData().getConfigurationSection("items")).getKeys(false));
        randomItemKey = itemKeys.get(new SecureRandom().nextInt(itemKeys.size()));

        String materialName = Data.getData().getString("items." + randomItemKey + ".material");
        String translatedMaterialName = Data.getData().getString("items." + randomItemKey + ".translated_material");

        if (!isItemAvailableInSeller(materialName)) {
            Bukkit.getLogger().warning("Event item '" + materialName + "' not available in the seller slots.");
            return;
        }

        isEventRunning = true;

        int currentPrice = Data.getData().getInt("items." + randomItemKey + ".price");
        int eventTime = Config.getConfig().getInt("event.bossbar.time");

        int minPercent = Config.getConfig().getInt("event.bossbar.percent.min");
        int maxPercent = Config.getConfig().getInt("event.bossbar.percent.max");

        Random random = new Random();
        int randomPercent = minPercent + random.nextInt(maxPercent - minPercent + 1);

        int newPrice = currentPrice + currentPrice * randomPercent / 100;
        double multiplier = Data.getData().getDouble("seller.multiplier");
        oldPrices.put(randomItemKey, currentPrice);
        Data.getData().set("items." + randomItemKey + ".price", newPrice);
        Data.saveData();
        if (materialName == null) {
            throw new NullPointerException("Material name is null");
        }
        assert translatedMaterialName != null;
        bossBar = Bukkit.createBossBar(HexColor.color(Objects.requireNonNull(Config.getConfig().getString("event.bossbar.message")).replace("%material%", translatedMaterialName).replace("%percent%", String.valueOf(randomPercent))), BarColor.valueOf(Config.getConfig().getString("event.bossbar.color")), BarStyle.valueOf(Config.getConfig().getString("event.bossbar.style")));

        new BukkitRunnable() {
            int timeLeft = eventTime;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    bossBar.removeAll();
                    Data.getData().set("items." + randomItemKey + ".price", oldPrices.get(randomItemKey));
                    Data.saveData();
                    isEventRunning = false;
                    eventManager.resetUpdateTime();
                    String topSeller;
                    double topSales = 0;
                    if (!playerSales.isEmpty()) {
                        topSeller = Collections.max(playerSales.entrySet(), Map.Entry.comparingByValue()).getKey();
                        double rawTopSales = playerSales.get(topSeller);
                        DecimalFormat df = new DecimalFormat("#.##");
                        topSales = Double.parseDouble(df.format(rawTopSales * multiplier));
                    } else {
                        topSeller = Config.getConfig().getString("event.replace.nick");
                    }
                    List<String> endEventMessages = Config.getConfig().getStringList("event.messages.event-end");
                    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                        for (String message : endEventMessages) {
                            assert topSeller != null;
                            onlinePlayer.sendMessage(HexColor.color(message.replace("%player%", topSeller).replace("%player_price%", String.valueOf(topSales)).replace("%material%", translatedMaterialName)));
                        }
                    }
                    assert topSeller != null;
                    if (!topSeller.equals(Config.getConfig().getString("event.replace.nick"))) {
                        List<String> rewardCommands = Config.getConfig().getStringList("event.give-reward");
                        for (String command : rewardCommands) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", topSeller));
                        }
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getOpenInventory().getTitle().equals(HexColor.color(invSeller.getTitle()))) {
                            onlinePlayer.closeInventory();
                        }
                    }
                    playerSales.clear();
                    cancel();
                } else {
                    bossBar.setProgress((double) timeLeft / eventTime);
                    timeLeft--;
                }
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }
}


