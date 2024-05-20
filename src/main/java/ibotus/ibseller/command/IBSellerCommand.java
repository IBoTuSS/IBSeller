package ibotus.ibseller.command;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.configurations.IBData;
import ibotus.ibseller.configurations.IBItems;
import ibotus.ibseller.eventmanager.IBEvent;
import ibotus.ibseller.inventories.IBInvSeller;
import ibotus.ibseller.utils.IBEventManager;
import ibotus.ibseller.utils.IBHexColor;
import ibotus.ibseller.utils.IBSellerUpdater;

import ibotus.ibseller.utils.IBUtils;
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
    private final IBInvSeller invSeller;
    private final IBSellerUpdater IBSellerUpdater;
    private final IBEvent ibEvent;

    public IBSellerCommand(JavaPlugin plugin, IBInvSeller invSeller, IBSellerUpdater IBSellerUpdater, IBEventManager ibeventmanager) {
        this.plugin = plugin;
        this.invSeller = invSeller;
        this.IBSellerUpdater = IBSellerUpdater;
        this.ibEvent = new IBEvent(plugin, invSeller, ibeventmanager);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("ibseller.reload")) {
                IBConfig.loadYaml(this.plugin);
                IBItems.loadYaml(this.plugin);
                sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.reload"))));
            } else {
                sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.permission"))));
            }
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("event-start")) {
            if (sender.hasPermission("ibseller.event-start")) {
                if (IBEvent.isEventRunning()) {
                    sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("event.messages.event-running"))));
                    return true;
                }
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getOpenInventory().getTitle().equals(IBHexColor.color(this.invSeller.getTitle()))) {
                        onlinePlayer.closeInventory();
                    }
                    String soundKey = "sound.event-start";
                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                    onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
                }
                this.ibEvent.startEvent();
                sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("event.messages.event-start"))));
                List<String> event = IBConfig.getConfig().getStringList("messages.seller-event");
                String translatedMaterialName = IBUtils.getTranslatedMaterialName(IBEvent.randomItemKey);
                for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                    for (String message : event) {
                        onlinePlayer.sendMessage(IBHexColor.color(message.replace("%material%", translatedMaterialName)));
                    }
                }
            } else {
                sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.permission"))));
            }
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("regenerate")) {
            if (sender.hasPermission("ibseller.regenerate")) {
                if (IBEvent.isEventRunning()) {
                    sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("event.messages.event-running"))));
                    return true;
                }
                this.IBSellerUpdater.resetUpdateTime();
                IBData.saveItems();
                List<String> updateMessages = IBConfig.getConfig().getStringList("messages.seller-update");
                for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                    for (String message : updateMessages) {
                        onlinePlayer.sendMessage(IBHexColor.color(message));
                    }
                }
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getOpenInventory().getTitle().equals(IBHexColor.color(this.invSeller.getTitle()))) {
                        onlinePlayer.closeInventory();
                    }
                    String soundKey = "sound.seller-update";
                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                    onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
                }
            } else {
                sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.permission"))));
            }
        }
        if (sender instanceof Player player) {
            if (args.length == 0 || args[0].equalsIgnoreCase("seller")) {
                if (player.hasPermission("ibseller.seller")) {
                    String soundKey = "sound.open-seller";
                    Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
                    float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
                    float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
                    player.playSound(player.getLocation(), sound, volume, pitch);
                    invSeller.openInventory(player, invSeller);
                } else {
                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.permission"))));
                }
            }
        }
        return true;
    }


    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("regenerate", "reload", "event-start");
        return null;
    }
}

