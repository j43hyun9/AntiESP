//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.listeners;

import com.Antiesp.AntiEsp.AntiEsp;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class PlayerListener implements Listener {
    private final AntiEsp plugin;

    public PlayerListener(AntiEsp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(
        priority = EventPriority.MONITOR
    )
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("玩家 " + player.getName() + " 加入服务器");
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
                this.plugin.getVisibilityManager().forceUpdatePlayer(player);
            }

        }, 40L);
    }

    @EventHandler(
        priority = EventPriority.MONITOR
    )
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("玩家 " + player.getName() + " 离开服务器");
        }

        this.plugin.getVisibilityManager().clearPlayerData(player);
    }

    @EventHandler(
        priority = EventPriority.MONITOR
    )
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("玩家 " + player.getName() + " 重生");
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
                this.plugin.getVisibilityManager().forceUpdatePlayer(player);
            }

        }, 20L);
    }

    @EventHandler(
        priority = EventPriority.MONITOR
    )
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.isDebugMode()) {
            Logger var10000 = this.plugin.getLogger();
            String var10001 = player.getName();
            var10000.info("玩家 " + var10001 + " " + (event.isSneaking() ? "开始潜行" : "停止潜行"));
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
                this.plugin.getVisibilityManager().forceUpdatePlayer(player);
            }

        }, 5L);
    }

    @EventHandler(
        priority = EventPriority.MONITOR
    )
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.isDebugMode()) {
            Logger var10000 = this.plugin.getLogger();
            String var10001 = player.getName();
            var10000.info("玩家 " + var10001 + " " + (event.isSprinting() ? "开始疾跑" : "停止疾跑"));
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
                this.plugin.getVisibilityManager().forceUpdatePlayer(player);
            }

        }, 5L);
    }
}
