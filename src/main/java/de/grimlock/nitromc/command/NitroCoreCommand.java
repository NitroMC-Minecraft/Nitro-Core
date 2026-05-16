package de.grimlock.nitromc.command;

import de.grimlock.nitromc.annotation.Command;
import de.grimlock.nitromc.config.ConfigService;
import de.grimlock.nitromc.service.impl.DiagnosticsService;
import de.grimlock.nitromc.service.impl.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Command(value = "nitrocore", description = "Main command for NitroCore", permission = "nitrocore.admin")
public class NitroCoreCommand extends AbstractCommand {

    private final DiagnosticsService diagnosticsService;
    private final ConfigService configService;

    @Inject
    public NitroCoreCommand(DiagnosticsService diagnosticsService, ConfigService configService) {
        this.diagnosticsService = diagnosticsService;
        this.configService = configService;

        registerSubcommand("diagnostic", this::diagnostic);
        registerSubcommand("reload", this::reload);
        registerSubcommand("info", this::info);
    }

    private boolean diagnostic(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("Starte Systemdiagnose...", NamedTextColor.YELLOW));

        diagnosticsService.runDiagnostics().thenAccept(report -> {
            if (report.success()) {
                sender.sendMessage(Component.text("Diagnose abgeschlossen: ALLE SYSTEME OK", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Diagnose abgeschlossen: FEHLER GEFUNDEN", NamedTextColor.RED));
            }

            for (String detail : report.details()) {
                sender.sendMessage(Component.text("- " + detail, NamedTextColor.GRAY));
            }
        });
        return true;
    }

    private boolean reload(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("Lade Konfigurationen neu...", NamedTextColor.YELLOW));
        try {
            configService.reloadConfigs();
            sender.sendMessage(Component.text("Konfigurationen erfolgreich neu geladen!", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Fehler beim Laden: " + e.getMessage(), NamedTextColor.RED));
        }
        return true;
    }

    private boolean info(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("=== NitroCore Info ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: 1.0-SNAPSHOT", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Status: RUNNING", NamedTextColor.GREEN));
        return true;
    }

    @Override
    protected String getCommandName() {
        return "nitrocore";
    }

    @Override
    protected Map<String, String> getSubcommandDescriptions() {
        Map<String, String> desc = new HashMap<>();
        desc.put("diagnostic", "Zeigt Systemdiagnose");
        desc.put("reload", "Lädt Konfigurationen neu");
        desc.put("info", "Zeigt Plugin-Info");
        return desc;
    }
}
