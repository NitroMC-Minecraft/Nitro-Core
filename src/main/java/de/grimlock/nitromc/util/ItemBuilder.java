package de.grimlock.nitromc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public ItemBuilder name(Component name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return this;
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return this;
        List<Component> formattedLore = new ArrayList<>();
        for (Component component : lore) {
            formattedLore.add(component.decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(formattedLore);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(Component... lore) {
        return lore(List.of(lore));
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return this;
        consumer.accept(meta);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder customModelData(int data) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return this;
        meta.setCustomModelData(data);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level, boolean ignoreRestrictions) {
        if (ignoreRestrictions) {
            itemStack.addUnsafeEnchantment(enchantment, level);
        } else {
            itemStack.addEnchantment(enchantment, level);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return this;
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder glow() {
        Enchantment glowEnchant = Enchantment.getByKey(NamespacedKey.minecraft("luck_of_the_sea"));
        if (glowEnchant == null) {
            glowEnchant = Enchantment.getByKey(NamespacedKey.minecraft("lure"));
        }
        if (glowEnchant != null) {
            enchant(glowEnchant, 1);
        }
        flags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return this;
        meta.setUnbreakable(unbreakable);
        itemStack.setItemMeta(meta);
        return this;
    }

    public <T, Z> ItemBuilder persistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return this;
        meta.getPersistentDataContainer().set(key, type, value);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder skullTexture(String base64) {
        if (!(itemStack.getItemMeta() instanceof SkullMeta skullMeta)) return this;

        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            String url = decoded.split("\"url\":\"")[1].split("\"")[0];
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "");
            profile.getTextures().setSkin(new URL(url));
            skullMeta.setOwnerProfile(profile);
            itemStack.setItemMeta(skullMeta);
        } catch (Exception e) {
            // Silent fail — invalid base64 or unexpected format
        }

        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}
