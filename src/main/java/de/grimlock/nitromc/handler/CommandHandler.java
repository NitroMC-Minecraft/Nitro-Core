package de.grimlock.nitromc.handler;

import com.google.inject.Injector;
import de.grimlock.nitromc.annotation.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandHandler {

    private final Injector injector;
    private CommandMap commandMap;

    @Inject
    public CommandHandler(Injector injector) {
        this.injector = injector;
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            this.commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void registerCommands(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Command.class);

        List<Class<?>> sorted = annotated.stream()
                .sorted(Comparator.comparingInt((Class<?> c) -> c.getAnnotation(Command.class).priority()).reversed())
                .collect(Collectors.toList());

        for (Class<?> clazz : sorted) {
            Command annotation = clazz.getAnnotation(Command.class);
            Object commandInstance = injector.getInstance(clazz);

            if (commandInstance instanceof org.bukkit.command.CommandExecutor) {
                DynamicCommand dynamicCommand = new DynamicCommand(
                        annotation.value(),
                        annotation.description(),
                        annotation.usage(),
                        Arrays.asList(annotation.aliases()),
                        (org.bukkit.command.CommandExecutor) commandInstance,
                        commandInstance instanceof TabCompleter ? (TabCompleter) commandInstance : null
                );
                if (!annotation.permission().isEmpty()) {
                    dynamicCommand.setPermission(annotation.permission());
                }
                commandMap.register("nitrocore", dynamicCommand);
            }
        }
    }

    private static class DynamicCommand extends BukkitCommand {
        private final org.bukkit.command.CommandExecutor executor;
        private final TabCompleter completer;

        protected DynamicCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull java.util.List<String> aliases, org.bukkit.command.CommandExecutor executor, TabCompleter completer) {
            super(name, description, usageMessage, aliases);
            this.executor = executor;
            this.completer = completer;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            return executor.onCommand(sender, this, commandLabel, args);
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            if (completer != null) {
                return completer.onTabComplete(sender, this, alias, args);
            }
            return Collections.emptyList();
        }
    }
}
