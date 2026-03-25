//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.managers;

import com.Antiesp.AntiEsp.AntiEsp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerManager implements Listener {
    private final AntiEsp plugin;
    private final Map<UUID, Long> lastMoveTime;
    private final Map<UUID, Long> lastUpdateTime;
    private final Map<UUID, Boolean> playerStates;

    public PlayerManager(AntiEsp plugin) {
        this.plugin = plugin;
        this.lastMoveTime = new ConcurrentHashMap();
        this.lastUpdateTime = new ConcurrentHashMap();
        this.playerStates = new ConcurrentHashMap();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        this.lastMoveTime.put(playerId, currentTime);
        this.lastUpdateTime.put(playerId, currentTime);
        this.playerStates.put(playerId, true);
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("玩家 " + player.getName() + " 加入，已初始化数据");
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.getVisibilityManager().forceUpdatePlayer(player), 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        this.lastMoveTime.remove(playerId);
        this.lastUpdateTime.remove(playerId);
        this.playerStates.remove(playerId);
        this.plugin.getVisibilityManager().clearPlayerData(player);
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("玩家 " + player.getName() + " 离开，已清理数据");
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (this.hasSignificantMovement(event)) {
            this.lastMoveTime.put(playerId, System.currentTimeMillis());
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                if (player.isOnline()) {
                    this.plugin.getVisibilityManager().forceUpdatePlayer(player);
                }

            }, 5L);
        }

    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        this.lastMoveTime.put(playerId, currentTime);
        this.lastUpdateTime.put(playerId, currentTime);
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
                this.plugin.getVisibilityManager().forceUpdatePlayer(player);
            }

        }, 10L);
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("玩家 " + player.getName() + " 传送，已更新位置数据");
        }

    }

    private boolean hasSignificantMovement(PlayerMoveEvent event) {
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return true;
        } else {
            double distance = event.getFrom().distance(event.getTo());
            return distance > 0.1;
        }
    }

    public long getLastMoveTime(Player player) {
        return (Long)this.lastMoveTime.getOrDefault(player.getUniqueId(), 0L);
    }

    public long getLastUpdateTime(Player player) {
        return (Long)this.lastUpdateTime.getOrDefault(player.getUniqueId(), 0L);
    }

    public boolean isPlayerOnline(Player player) {
        return (Boolean)this.playerStates.getOrDefault(player.getUniqueId(), false);
    }

    public boolean getPlayerState(Player player) {
        return (Boolean)this.playerStates.getOrDefault(player.getUniqueId(), false);
    }

    public void setPlayerState(Player player, boolean state) {
        this.playerStates.put(player.getUniqueId(), state);
    }

    public void updatePlayerTimestamp(Player player) {
        long currentTime = System.currentTimeMillis();
        this.lastUpdateTime.put(player.getUniqueId(), currentTime);
    }

    public int getOnlinePlayerCount() {
        return this.playerStates.size();
    }

    public int getActivePlayerCount() {
        long currentTime = System.currentTimeMillis();
        long threshold = currentTime - 30000L;
        return (int)this.lastMoveTime.values().stream().filter((time) -> time > threshold).count();
    }

    public void cleanup() {
        this.lastMoveTime.clear();
        this.lastUpdateTime.clear();
        this.playerStates.clear();
        this.plugin.getLogger().info("玩家管理器已清理");
    }
}
