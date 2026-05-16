package de.grimlock.nitromc.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemBuilderTest {

    @Test
    void testBuildMaterial() {
        // Since Material and ItemStack are Bukkit classes, they might need a mock environment
        // but for pure logic, we can test basic instantiation if not blocked by Bukkit's static init.
        // Usually, for ItemBuilder, we test if the meta is applied.
    }
}
