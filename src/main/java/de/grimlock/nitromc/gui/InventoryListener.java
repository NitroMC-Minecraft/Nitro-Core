package de.grimlock.nitromc.gui;

import de.grimlock.nitromc.annotation.AutoListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import javax.inject.Singleton;

@AutoListener
@Singleton
public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof AbstractInventory) {
            ((AbstractInventory) event.getInventory().getHolder()).handleAction(event);
        }
    }
}
