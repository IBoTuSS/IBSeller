package ibotus.ibseller.utils;

import ibotus.ibseller.configurations.IBConfig;
import ibotus.ibseller.eventmanager.IBEvent;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class IBSellerPlaceholders extends PlaceholderExpansion {

    private final IBSellerUpdater ibSellerUpdater;
    private final IBEventManager ibEventManager;

    public IBSellerPlaceholders(IBSellerUpdater ibSellerUpdater, IBEventManager ibeventmanager) {
        this.ibSellerUpdater = ibSellerUpdater;
        this.ibEventManager = ibeventmanager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "IBSeller";
    }

    @Override
    public @NotNull String getAuthor() {
        return "IBoTuS";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.9";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if ("time".equals(identifier)) {
            return ibSellerUpdater.getRemainingTime();
        }

        if ("event".equals(identifier)) {
            if (IBEvent.isEventRunning) {
                return IBHexColor.color(IBConfig.getConfig().getString("event.replace.event-active"));
            } else {
                return ibEventManager.getRemainingTime();
            }
        }

        return null;
    }
}

