package de.grimlock.nitromc.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PreDataLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final UUID uuid;

    public PreDataLoadEvent(UUID uuid) {
        super(true); // Async
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
