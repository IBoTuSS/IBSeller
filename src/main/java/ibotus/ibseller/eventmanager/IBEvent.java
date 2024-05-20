package ibotus.ibseller.eventmanager;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.configurations.IBData;
import ibotus.ibseller.inventories.IBInvSeller;
import ibotus.ibseller.utils.IBEventManager;
import ibotus.ibseller.utils.IBHexColor;

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

public class IBEvent {
    private final JavaPlugin plugin;
    private final IBInvSeller invSeller;
    private final HashMap<String, Integer> oldPrices = new HashMap<>();
    public static final HashMap<String, Integer> playerSales = new HashMap<>();
    public static final HashMap<String, Integer> playerTotalSales = new HashMap<>();
    public static boolean isEventRunning = false;
    public static String randomItemKey;
    public static BossBar bossBar;
    private final IBEventManager ibEventManager;


    public static boolean isEventRunning() {
        return isEventRunning;
    }

    public IBEvent(JavaPlugin plugin, IBInvSeller invSeller,IBEventManager ibeventmanager) {
        this.plugin = plugin;
        this.invSeller = invSeller;
        this.ibEventManager = ibeventmanager;
    }

    public static boolean isEventItem(ItemStack item) {
        String currentItemMaterialName = item.getType().name();

        String eventItemMaterialName = IBData.getData().getString("items." + randomItemKey + ".material");

        return currentItemMaterialName.equals(eventItemMaterialName);
    }

    public boolean isItemAvailableInSeller(String itemMaterialName) {
        for (String key : Objects.requireNonNull(IBData.getData().getConfigurationSection("items")).getKeys(false)) {
            String materialName = IBData.getData().getString("items." + key + ".material");
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

        List<String> itemKeys = new ArrayList<>(Objects.requireNonNull(IBData.getData().getConfigurationSection("items")).getKeys(false));
        randomItemKey = itemKeys.get(new SecureRandom().nextInt(itemKeys.size()));

        String materialName = IBData.getData().getString("items." + randomItemKey + ".material");
        String translatedMaterialName = IBData.getData().getString("items." + randomItemKey + ".translated_material");

        if (!isItemAvailableInSeller(materialName)) {
            Bukkit.getLogger().warning("Event item '" + materialName + "' not available in the seller slots.");
            return;
        }

        isEventRunning = true;

        int currentPrice = IBData.getData().getInt("items." + randomItemKey + ".price");
        int eventTime = IBConfig.getConfig().getInt("event.bossbar.time");

        int minPercent = IBConfig.getConfig().getInt("event.bossbar.percent.min");
        int maxPercent = IBConfig.getConfig().getInt("event.bossbar.percent.max");

        Random random = new Random();

        int randomPercent = minPercent + random.nextInt(maxPercent - minPercent + 1);

        int newPrice = currentPrice + currentPrice * randomPercent / 100;
        double multiplier = IBData.getData().getDouble("seller.multiplier");
        oldPrices.put(randomItemKey, currentPrice);
        IBData.getData().set("items." + randomItemKey + ".price", newPrice);
        IBData.saveData();
        if (materialName == null) {
            throw new NullPointerException("Material name is null");
        }
        assert translatedMaterialName != null;
        bossBar = Bukkit.createBossBar(IBHexColor.color(Objects.requireNonNull(IBConfig.getConfig().getString("event.bossbar.message"))
                        .replace("%material%", translatedMaterialName)
                        .replace("%percent%", String.valueOf(randomPercent))),
                BarColor.valueOf(IBConfig.getConfig().getString("event.bossbar.color")),
                BarStyle.valueOf(IBConfig.getConfig().getString("event.bossbar.style")));

        new BukkitRunnable() {
            int timeLeft = eventTime;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    bossBar.removeAll();
                    IBData.getData().set("items." + randomItemKey + ".price", oldPrices.get(randomItemKey));
                    IBData.saveData();
                    isEventRunning = false;
                    ibEventManager.resetUpdateTime();
                    String topSeller;
                    double topSales;
                    if (!playerSales.isEmpty()) {
                        topSeller = Collections.max(playerTotalSales.entrySet(), Map.Entry.comparingByValue()).getKey();
                        double rawTopSales = playerTotalSales.get(topSeller);
                        DecimalFormat df = new DecimalFormat("#.##");
                        topSales = Double.parseDouble(df.format(rawTopSales * multiplier));
                    } else {
                        topSeller = IBConfig.getConfig().getString("event.replace.nick");
                        topSales = 0;
                    }
                    List<String> endEventMessages = IBConfig.getConfig().getStringList("event.messages.event-end");
                    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                        for (String message : endEventMessages) {
                            assert topSeller != null;
                            onlinePlayer.sendMessage(IBHexColor.color(message.replace("%player%", topSeller).replace("%player_price%", String.valueOf(topSales)).replace("%material%", translatedMaterialName)));
                        }
                    }
                    assert topSeller != null;
                    if (!topSeller.equals(IBConfig.getConfig().getString("event.replace.nick"))) {
                        List<String> rewardCommands = IBConfig.getConfig().getStringList("event.give-reward");
                        for (String command : rewardCommands) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", topSeller));
                        }
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getOpenInventory().getTitle().equals(IBHexColor.color(invSeller.getTitle()))) {
                            onlinePlayer.closeInventory();
                        }
                    }
                    playerSales.clear();
                    playerTotalSales.clear();
                    this.cancel();
                } else {
                    bossBar.setProgress((double) timeLeft / eventTime);
                    timeLeft--;
                }
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }
}

