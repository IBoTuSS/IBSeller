package ibotus.ibseller.command;

import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.configurations.Items;
import ibotus.ibseller.events.SellerEventListener;
import ibotus.ibseller.inventories.InventorySeller;
import ibotus.ibseller.utils.EventManager;
import ibotus.ibseller.utils.HexColor;
import ibotus.ibseller.utils.SellerUpdater;

import ibotus.ibseller.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class IBSellerCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final InventorySeller invSeller;
    private final SellerUpdater SellerUpdater;
    private final SellerEventListener sellerEventListener;

    public IBSellerCommand(JavaPlugin plugin, InventorySeller invSeller, SellerUpdater SellerUpdater, EventManager ibeventmanager) {
        this.plugin = plugin;
        this.invSeller = invSeller;
        this.SellerUpdater = SellerUpdater;
        this.sellerEventListener = new SellerEventListener(plugin, invSeller, ibeventmanager);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (player.hasPermission("ibseller.seller")) {
                    String soundKey = "sound.open-seller";
                    Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
                    float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
                    float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");
                    player.playSound(player.getLocation(), sound, volume, pitch);
                    invSeller.openInventory(player, invSeller);
                } else {
                    player.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("messages.permission"))));
                }
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("ibseller.reload")) {
                    Config.loadYaml(this.plugin);
                    Data.loadYaml(this.plugin);
                    Items.loadYaml(this.plugin);
                    sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("messages.reload"))));
                } else {
                    sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("messages.permission"))));
                }
                break;
            case "event-start":
                if (sender.hasPermission("ibseller.event-start")) {
                    if (SellerEventListener.isEventRunning()) {
                        sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("event.messages.event-running"))));
                        return true;
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getOpenInventory().getTitle().equals(HexColor.color(this.invSeller.getTitle()))) {
                            onlinePlayer.closeInventory();
                        }
                        String soundKey = "sound.event-start";
                        Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
                        float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
                        float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");
                        onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
                    }
                    this.sellerEventListener.startEvent();
                    sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("event.messages.event-start"))));
                    List<String> event = Config.getConfig().getStringList("messages.seller-event");
                    String translatedMaterialName = Utils.getTranslatedMaterialName(SellerEventListener.randomItemKey);
                    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                        for (String message : event) {
                            onlinePlayer.sendMessage(HexColor.color(message.replace("%material%", translatedMaterialName)));
                        }
                    }
                } else {
                    sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("messages.permission"))));
                }
                break;
            case "regenerate":
                if (sender.hasPermission("ibseller.regenerate")) {
                    if (SellerEventListener.isEventRunning()) {
                        sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("event.messages.event-running"))));
                        return true;
                    }
                    this.SellerUpdater.resetUpdateTime();
                    Data.saveItems();
                    List<String> updateMessages = Config.getConfig().getStringList("messages.seller-update");
                    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                        for (String message : updateMessages) {
                            onlinePlayer.sendMessage(HexColor.color(message));
                        }
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getOpenInventory().getTitle().equals(HexColor.color(this.invSeller.getTitle()))) {
                            onlinePlayer.closeInventory();
                        }
                        String soundKey = "sound.seller-update";
                        Sound sound = Sound.valueOf(Config.getConfig().getString(soundKey + ".sound"));
                        float volume = (float) Config.getConfig().getDouble(soundKey + ".volume");
                        float pitch = (float) Config.getConfig().getDouble(soundKey + ".pitch");
                        onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
                    }
                } else {
                    sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("messages.permission"))));
                }
                break;
            default:
                sender.sendMessage(Objects.requireNonNull(HexColor.color(Config.getConfig().getString("messages.usage"))));
                break;
        }
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("regenerate", "reload", "event-start");
        return null;
    }

}


