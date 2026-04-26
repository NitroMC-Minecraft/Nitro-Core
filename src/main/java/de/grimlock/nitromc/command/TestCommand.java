package de.grimlock.nitromc.command;

import de.grimlock.nitromc.annotation.Command;
import de.grimlock.nitromc.service.impl.MessageService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

@Command(value = "testcore", description = "A test command for NitroCore", permission = "nitrocore.test")
public class TestCommand implements CommandExecutor {

    private final MessageService messageService;

    @Inject
    public TestCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        messageService.broadcast("The NitroCore system is working correctly!");
        return true;
    }
}
