package de.grimlock.nitromc.listener;

import de.grimlock.nitromc.annotation.AutoListener;
import de.grimlock.nitromc.service.impl.MessageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.inject.Inject;

@AutoListener
public class TestListener implements Listener {

    private final MessageService messageService;

    @Inject
    public TestListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        messageService.broadcast("Welcome " + event.getPlayer().getName() + " to the server!");
    }
}
