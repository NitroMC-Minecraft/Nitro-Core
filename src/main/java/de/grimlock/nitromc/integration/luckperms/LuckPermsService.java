package de.grimlock.nitromc.integration.luckperms;

import de.grimlock.nitromc.service.IService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    public boolean isAvailable() {
        return luckPerms != null;
    }

    public Optional<User> getUser(UUID uuid) {
        if (!isAvailable()) return Optional.empty();
        return Optional.ofNullable(luckPerms.getUserManager().getUser(uuid));
    }

    public CompletableFuture<Optional<User>> getUserAsync(UUID uuid) {
        if (!isAvailable()) return CompletableFuture.completedFuture(Optional.empty());
        return luckPerms.getUserManager()
                .loadUser(uuid)
                .thenApply(Optional::ofNullable);
    }

    public String getPrimaryGroup(Player player) {
        return getUser(player.getUniqueId())
                .map(User::getPrimaryGroup)
                .orElse("default");
    }

    public boolean hasPermission(Player player, String permission) {
        return getUser(player.getUniqueId())
                .map(user -> user.getCachedData()
                        .getPermissionData()
                        .checkPermission(permission)
                        .asBoolean())
                .orElse(player.hasPermission(permission));
    }

    public List<String> getAllGroups(Player player) {
        return getUser(player.getUniqueId())
                .map(user -> user.getNodes(NodeType.INHERITANCE).stream()
                        .map(InheritanceNode::getGroupName)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    public String getPrefix(Player player) {
        return getUser(player.getUniqueId())
                .map(user -> {
                    CachedMetaData meta = user.getCachedData().getMetaData();
                    return meta.getPrefix() != null ? meta.getPrefix() : "";
                })
                .orElse("");
    }

    public String getSuffix(Player player) {
        return getUser(player.getUniqueId())
                .map(user -> {
                    CachedMetaData meta = user.getCachedData().getMetaData();
                    return meta.getSuffix() != null ? meta.getSuffix() : "";
                })
                .orElse("");
    }

    public Optional<String> getMetaValue(Player player, String key) {
        return getUser(player.getUniqueId())
                .map(user -> Optional.ofNullable(user.getCachedData().getMetaData().getMetaValue(key)))
                .orElse(Optional.empty());
    }

    public CompletableFuture<Void> addToGroup(UUID uuid, String groupName) {
        if (!isAvailable()) return CompletableFuture.completedFuture(null);
        return luckPerms.getUserManager().modifyUser(uuid, user ->
                user.data().add(InheritanceNode.builder(groupName).build())
        );
    }

    public CompletableFuture<Void> removeFromGroup(UUID uuid, String groupName) {
        if (!isAvailable()) return CompletableFuture.completedFuture(null);
        return luckPerms.getUserManager().modifyUser(uuid, user ->
                user.data().remove(InheritanceNode.builder(groupName).build())
        );
    }
}
