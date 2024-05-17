package ibotus.ibseller.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class IBCustomHead {

    public static ItemStack getSkull(String url, String path) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        if (Bukkit.getBukkitVersion().contains("1.12")) {
            item = new ItemStack(Material.valueOf("HEAD"));
        }
        ItemMeta meta = item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        try {
            assert (meta != null);
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        }
        catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException var6) {
            Bukkit.getLogger().severe("Такой головы не существует: " + path);
        }
        item.setItemMeta(meta);
        return item;
    }
}
