package de.grimlock.nitromc.integration.luckperms;

import de.grimlock.nitromc.service.IService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class LuckPermsService implements IService {

    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            this.luckPerms = LuckPermsProvider.get();
        }
    }

    @Override
    public void onDisable() {}

    public Optional<User> getUser(UUID uuid) {
        if (luckPerms == null) return Optional.empty();
        return Optional.ofNullable(luckPerms.getUserManager().getUser(uuid));
    }

    public String getPrimaryGroup(Player player) {
        return getUser(player.getUniqueId())
                .map(User::getPrimaryGroup)
                .orElse("default");
    }

    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
}
