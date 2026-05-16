package de.grimlock.nitromc.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {

    protected final Map<String, BiFunction<CommandSender, String[], Boolean>> subcommands = new HashMap<>();

    protected void registerSubcommand(String name, BiFunction<CommandSender, String[], Boolean> handler) {
        subcommands.put(name.toLowerCase(), handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String subcommand = args[0].toLowerCase();
        BiFunction<CommandSender, String[], Boolean> handler = subcommands.get(subcommand);

        if (handler == null) {
            sender.sendMessage("§cUnknown subcommand: " + args[0]);
            return showHelp(sender);
        }

        return handler.apply(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(subcommands.keySet());
            completions.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return completions;
        }
        return Collections.emptyList();
    }

    protected boolean showHelp(CommandSender sender) {
        sender.sendMessage("§e=== " + getCommandName() + " ===");
        getSubcommandDescriptions().forEach((cmd, desc) ->
            sender.sendMessage("§6/" + getCommandName() + " " + cmd + " §7- " + desc)
        );
        return true;
    }

    protected abstract String getCommandName();

    protected abstract Map<String, String> getSubcommandDescriptions();
}
