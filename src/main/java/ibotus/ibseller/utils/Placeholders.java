package ibotus.ibseller.utils;

import ibotus.ibseller.configurations.Config;
import ibotus.ibseller.events.SellerEventListener;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Placeholders extends PlaceholderExpansion {

    private final SellerUpdater sellerUpdater;
    private final EventManager eventManager;

    public Placeholders(SellerUpdater sellerUpdater, EventManager ibeventmanager) {
        this.sellerUpdater = sellerUpdater;
        this.eventManager = ibeventmanager;
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
        return "2.4";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if ("time".equals(identifier)) {
            return sellerUpdater.getRemainingTime();
        }

        if ("event".equals(identifier)) {
            if (SellerEventListener.isEventRunning) {
                return HexColor.color(Objects.requireNonNull(Config.getConfig().getString("event.replace.event-active")));
            } else {
                return eventManager.getRemainingTime();
            }
        }

        return null;
    }
}

