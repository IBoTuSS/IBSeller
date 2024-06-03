package ibotus.ibseller;

import ibotus.ibseller.command.IBSellerCommand;
import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.configurations.IBData;
import ibotus.ibseller.configurations.IBItems;
import ibotus.ibseller.eventmanager.IBClickEvent;
import ibotus.ibseller.events.IBInventoryClose;
import ibotus.ibseller.events.IBPlayerJoinListener;
import ibotus.ibseller.inventories.*;
import ibotus.ibseller.utils.*;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class IBSeller extends JavaPlugin {
    private static Economy econ = null;

    private void msg(String msg) {
        String prefix = IBHexColor.color("&aIBSeller &7| ");
        Bukkit.getConsoleSender().sendMessage(IBHexColor.color(prefix + msg));
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Не найдена зависимость Vault!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        IBData.loadYaml(this);
        IBConfig.loadYaml(this);
        IBItems.loadYaml(this);
        IBData.saveItems();

        IBSellerUpdater IBSellerUpdater = new IBSellerUpdater(this);
        IBInvSeller invSeller = new IBInvSeller(IBSellerUpdater, this);
        IBInventoryClose ibInventoryClose = new IBInventoryClose(invSeller);
        IBUtils IBUtils = new IBUtils(IBSellerUpdater, invSeller);
        IBEventManager IBEventManager = new IBEventManager(this, invSeller);
        IBSellerCommand sellerCommand = new IBSellerCommand(this, invSeller, IBSellerUpdater, IBEventManager);

        getServer().getPluginManager().registerEvents(new IBInvSellerListener(econ, invSeller), this);
        getServer().getPluginManager().registerEvents(new IBClickEvent(econ, IBUtils), this);
        getServer().getPluginManager().registerEvents(new IBPlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(ibInventoryClose, this);

        Objects.requireNonNull(getCommand("seller")).setExecutor(sellerCommand);
        Objects.requireNonNull(getCommand("seller")).setTabCompleter(sellerCommand);

        IBSellerUpdater.start(invSeller);
        IBEventManager.startEventTimer();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new IBSellerPlaceholders(IBSellerUpdater, IBEventManager).register();
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
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                econ = rsp.getProvider();
                return true;
            }
        }
    }
}
