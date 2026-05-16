package de.grimlock.nitromc.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {

    protected final Map<String, SubcommandEntry> subcommands = new LinkedHashMap<>();

    protected record SubcommandEntry(
        BiFunction<CommandSender, String[], Boolean> executor,
        TabCompleter tabCompleter
    ) {
        public SubcommandEntry(BiFunction<CommandSender, String[], Boolean> executor) {
            this(executor, null);
        }
    }

    protected void registerSubcommand(String name, BiFunction<CommandSender, String[], Boolean> handler) {
        registerSubcommand(name, handler, null);
    }

    protected void registerSubcommand(String name, BiFunction<CommandSender, String[], Boolean> handler, TabCompleter tabCompleter) {
        subcommands.put(name.toLowerCase(), new SubcommandEntry(handler, tabCompleter));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String subcommand = args[0].toLowerCase();
        SubcommandEntry entry = subcommands.get(subcommand);

        if (entry == null) {
            sender.sendMessage("§cUnknown subcommand: " + args[0]);
            return showHelp(sender);
        }

        String permission = requiredPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        return entry.executor().apply(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(subcommands.keySet());
            completions.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return completions;
        }

        if (args.length >= 2) {
            SubcommandEntry entry = subcommands.get(args[0].toLowerCase());
            if (entry != null && entry.tabCompleter() != null) {
                return entry.tabCompleter().onTabComplete(sender, command, label, args);
            }
        }

        return Collections.emptyList();
    }

    protected String requiredPermission() {
        return null;
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
