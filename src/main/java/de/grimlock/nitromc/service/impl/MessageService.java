package de.grimlock.nitromc.service.impl;

import de.grimlock.nitromc.service.IService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class MessageService implements IService {

    private final Component prefix = Component.text("[NitroCore] ", NamedTextColor.DARK_RED);

    @Override
    public void onEnable() {
        // Initialization logic
    }

    @Override
    public void onDisable() {
        // Cleanup logic
    }

    public void broadcast(String message) {
        Bukkit.broadcast(prefix.append(Component.text(message, NamedTextColor.GRAY)));
    }
}
