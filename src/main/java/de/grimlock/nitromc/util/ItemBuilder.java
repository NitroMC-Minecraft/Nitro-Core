package de.grimlock.nitromc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
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
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        ItemMeta meta = itemStack.getItemMeta();
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
        consumer.accept(meta);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}
