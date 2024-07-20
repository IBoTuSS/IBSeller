package ibotus.ibseller;

import ibotus.ibseller.command.IBSellerCommand;
import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.configurations.Data;
import ibotus.ibseller.configurations.Items;
import ibotus.ibseller.events.InventoryClickListener;
import ibotus.ibseller.events.InventoryCloseListener;
import ibotus.ibseller.events.InventorySellerListener;
import ibotus.ibseller.events.PlayerJoinListener;
import ibotus.ibseller.inventories.*;
import ibotus.ibseller.utils.*;

import net.milkbowl.vault.economy.Economy;

import org.black_ixx.playerpoints.PlayerPoints;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class IBSeller extends JavaPlugin {
    private static Economy econ = null;
    private static PlayerPoints playerPoints = null;

    private void msg(String msg) {
        String prefix = HexColor.color("&aIBSeller &7| ");
        Bukkit.getConsoleSender().sendMessage(HexColor.color(prefix + msg));
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault не установлен!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!hookPlayerPoints()) {
            getLogger().warning("PlayerPoints не установлен!");
        }

        Data.loadYaml(this);
        Config.loadYaml(this);
        Items.loadYaml(this);
        Data.saveItems();

        SellerUpdater SellerUpdater = new SellerUpdater(this);
        InventorySeller invSeller = new InventorySeller(SellerUpdater, this);
        InventoryCloseListener inventoryCloseListener = new InventoryCloseListener(invSeller);
        Utils Utils = new Utils(SellerUpdater, invSeller);
        EventManager EventManager = new EventManager(this, invSeller);
        IBSellerCommand sellerCommand = new IBSellerCommand(this, invSeller, SellerUpdater, EventManager);

        getServer().getPluginManager().registerEvents(new InventorySellerListener(econ, invSeller), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(econ, playerPoints, Utils), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(inventoryCloseListener, this);

        Objects.requireNonNull(getCommand("seller")).setExecutor(sellerCommand);
        Objects.requireNonNull(getCommand("seller")).setTabCompleter(sellerCommand);

        SellerUpdater.start(invSeller);
        EventManager.startEventTimer();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(SellerUpdater, EventManager).register();
        }

        Bukkit.getConsoleSender().sendMessage("");
        msg("&fDeveloper: &aIBoTuS");
        msg("&fVersion: &dv" + this.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("");
        msg("&fDisable plugin.");
        Bukkit.getConsoleSender().sendMessage("");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private boolean hookPlayerPoints() {
        final Plugin plugin = this.getServer().getPluginManager().getPlugin("PlayerPoints");
        playerPoints = (PlayerPoints) plugin;
        return playerPoints != null;
    }
}
