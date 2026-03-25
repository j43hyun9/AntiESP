//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.managers;

import com.Antiesp.AntiEsp.AntiEsp;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PacketManager implements Listener {
    private final AntiEsp plugin;
    private final Map<UUID, Set<UUID>> hiddenPlayers;
    private final Map<UUID, Set<UUID>> visiblePlayers;
    private Class<?> packetPlayOutEntityDestroyClass;
    private Class<?> packetPlayOutNamedEntitySpawnClass;
    private Class<?> packetPlayOutEntityMetadataClass;
    private Class<?> packetPlayOutEntityEquipmentClass;
    private Class<?> craftPlayerClass;
    private Class<?> entityPlayerClass;
    private Class<?> playerConnectionClass;
    private Method sendPacketMethod;
    private Method getHandleMethod;
    private Constructor<?> destroyPacketConstructor;
    private Constructor<?> spawnPacketConstructor;
    private boolean nmsAvailable = false;

    public PacketManager(AntiEsp plugin) {
        this.plugin = plugin;
        this.hiddenPlayers = new ConcurrentHashMap();
        this.visiblePlayers = new ConcurrentHashMap();
        this.initializeNMS();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("数据包管理器初始化完成（使用NMS反射，保持tab栏显示）");
    }

    private void initializeNMS() {
        try {
            String version = this.getServerVersion();
            String nmsPackage = "net.minecraft.server." + version;
            String craftPackage = "org.bukkit.craftbukkit." + version;
            this.packetPlayOutEntityDestroyClass = Class.forName(nmsPackage + ".PacketPlayOutEntityDestroy");
            this.packetPlayOutNamedEntitySpawnClass = Class.forName(nmsPackage + ".PacketPlayOutNamedEntitySpawn");
            this.packetPlayOutEntityMetadataClass = Class.forName(nmsPackage + ".PacketPlayOutEntityMetadata");
            this.packetPlayOutEntityEquipmentClass = Class.forName(nmsPackage + ".PacketPlayOutEntityEquipment");
            this.craftPlayerClass = Class.forName(craftPackage + ".entity.CraftPlayer");
            this.entityPlayerClass = Class.forName(nmsPackage + ".EntityPlayer");
            this.playerConnectionClass = Class.forName(nmsPackage + ".PlayerConnection");
            this.getHandleMethod = this.craftPlayerClass.getMethod("getHandle");
            this.sendPacketMethod = this.playerConnectionClass.getMethod("a", Class.forName(nmsPackage + ".Packet"));
            this.destroyPacketConstructor = this.packetPlayOutEntityDestroyClass.getConstructor(int[].class);
            this.spawnPacketConstructor = this.packetPlayOutNamedEntitySpawnClass.getConstructor(this.entityPlayerClass);
            this.nmsAvailable = true;
            this.plugin.getLogger().info("NMS反射初始化成功，版本: " + version);
        } catch (Exception e) {
            this.nmsAvailable = false;
            this.plugin.getLogger().warning("NMS反射初始化失败，将使用Bukkit API: " + e.getMessage());
            if (this.plugin.isDebugMode()) {
                e.printStackTrace();
            }
        }

    }

    private String getServerVersion() {
        String packageName = this.plugin.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf(46) + 1);
    }

    public void hidePlayer(UUID viewer, UUID target) {
        ((Set)this.hiddenPlayers.computeIfAbsent(viewer, (k) -> ConcurrentHashMap.newKeySet())).add(target);
        ((Set)this.visiblePlayers.computeIfAbsent(viewer, (k) -> ConcurrentHashMap.newKeySet())).remove(target);
        Player viewerPlayer = this.plugin.getServer().getPlayer(viewer);
        Player targetPlayer = this.plugin.getServer().getPlayer(target);
        if (viewerPlayer != null && targetPlayer != null) {
            this.sendDestroyEntityPacket(viewerPlayer, targetPlayer);
            if (this.plugin.isDebugMode()) {
                Logger var10000 = this.plugin.getLogger();
                String var10001 = viewerPlayer.getName();
                var10000.info("玩家 " + var10001 + " 隐藏了玩家 " + targetPlayer.getName() + " (NMS数据包，保持tab栏显示)");
            }
        }

    }

    private void sendDestroyEntityPacket(Player viewer, Player target) {
        try {
            if (this.nmsAvailable && this.destroyPacketConstructor != null && this.sendPacketMethod != null) {
                Object destroyPacket = this.destroyPacketConstructor.newInstance(new int[]{target.getEntityId()});
                Object entityPlayer = this.getHandleMethod.invoke(viewer);
                Object playerConnection = entityPlayer.getClass().getField("b").get(entityPlayer);
                this.sendPacketMethod.invoke(playerConnection, destroyPacket);
                if (this.plugin.isDebugMode()) {
                    Logger var10000 = this.plugin.getLogger();
                    String var10001 = target.getName();
                    var10000.info("使用NMS发送销毁实体数据包: " + var10001 + " 对 " + viewer.getName());
                }
            } else {
                viewer.hidePlayer(this.plugin, target);
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().warning("NMS不可用，回退到Bukkit API（tab栏会消失）");
                }
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("发送销毁实体数据包时发生错误: " + e.getMessage());
            }

            viewer.hidePlayer(this.plugin, target);
        }

    }

    public void showPlayer(UUID viewer, UUID target) {
        ((Set)this.hiddenPlayers.computeIfAbsent(viewer, (k) -> ConcurrentHashMap.newKeySet())).remove(target);
        ((Set)this.visiblePlayers.computeIfAbsent(viewer, (k) -> ConcurrentHashMap.newKeySet())).add(target);
        Player viewerPlayer = this.plugin.getServer().getPlayer(viewer);
        Player targetPlayer = this.plugin.getServer().getPlayer(target);
        if (viewerPlayer != null && targetPlayer != null) {
            this.sendSpawnEntityPacket(viewerPlayer, targetPlayer);
            if (this.plugin.isDebugMode()) {
                Logger var10000 = this.plugin.getLogger();
                String var10001 = viewerPlayer.getName();
                var10000.info("玩家 " + var10001 + " 显示了玩家 " + targetPlayer.getName() + " (NMS重新生成实体)");
            }
        }

    }

    private void sendSpawnEntityPacket(Player viewer, Player target) {
        try {
            if (this.nmsAvailable && this.spawnPacketConstructor != null && this.sendPacketMethod != null) {
                Object targetEntityPlayer = this.getHandleMethod.invoke(target);
                Object spawnPacket = this.spawnPacketConstructor.newInstance(targetEntityPlayer);
                Object entityPlayer = this.getHandleMethod.invoke(viewer);
                Object playerConnection = entityPlayer.getClass().getField("b").get(entityPlayer);
                this.sendPacketMethod.invoke(playerConnection, spawnPacket);
                this.sendEntityMetadataPacket(viewer, target);
                this.sendEntityEquipmentPacket(viewer, target);
                if (this.plugin.isDebugMode()) {
                    Logger var10000 = this.plugin.getLogger();
                    String var10001 = target.getName();
                    var10000.info("使用NMS重新生成玩家实体: " + var10001 + " 对 " + viewer.getName());
                }
            } else {
                viewer.showPlayer(this.plugin, target);
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().warning("NMS不可用，回退到Bukkit API");
                }
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("发送生成实体数据包时发生错误: " + e.getMessage());
            }

            viewer.showPlayer(this.plugin, target);
        }

    }

    private void sendEntityMetadataPacket(Player viewer, Player target) {
        try {
            if (this.nmsAvailable && this.packetPlayOutEntityMetadataClass != null && this.sendPacketMethod != null && this.plugin.isDebugMode()) {
                Logger var10000 = this.plugin.getLogger();
                String var10001 = target.getName();
                var10000.info("发送实体元数据包: " + var10001 + " 对 " + viewer.getName());
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("发送实体元数据包时发生错误: " + e.getMessage());
            }
        }

    }

    private void sendEntityEquipmentPacket(Player viewer, Player target) {
        try {
            if (this.nmsAvailable && this.packetPlayOutEntityEquipmentClass != null && this.sendPacketMethod != null && this.plugin.isDebugMode()) {
                Logger var10000 = this.plugin.getLogger();
                String var10001 = target.getName();
                var10000.info("发送实体装备包: " + var10001 + " 对 " + viewer.getName());
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("发送实体装备包时发生错误: " + e.getMessage());
            }
        }

    }

    public boolean isPlayerHidden(UUID viewer, UUID target) {
        return ((Set)this.hiddenPlayers.getOrDefault(viewer, ConcurrentHashMap.newKeySet())).contains(target);
    }

    public boolean isPlayerVisible(UUID viewer, UUID target) {
        return ((Set)this.visiblePlayers.getOrDefault(viewer, ConcurrentHashMap.newKeySet())).contains(target);
    }

    public void clearPlayerData(UUID playerId) {
        Player player = this.plugin.getServer().getPlayer(playerId);
        if (player != null) {
            for(Player otherPlayer : this.plugin.getServer().getOnlinePlayers()) {
                if (this.isPlayerHidden(playerId, otherPlayer.getUniqueId())) {
                    this.showPlayer(playerId, otherPlayer.getUniqueId());
                }
            }
        }

        this.hiddenPlayers.remove(playerId);
        this.visiblePlayers.remove(playerId);
        this.hiddenPlayers.values().forEach((set) -> set.remove(playerId));
        this.visiblePlayers.values().forEach((set) -> set.remove(playerId));
    }

    public int getHiddenPlayerCount(UUID viewer) {
        return ((Set)this.hiddenPlayers.getOrDefault(viewer, ConcurrentHashMap.newKeySet())).size();
    }

    public int getVisiblePlayerCount(UUID viewer) {
        return ((Set)this.visiblePlayers.getOrDefault(viewer, ConcurrentHashMap.newKeySet())).size();
    }

    public void updateAllPlayerVisibility() {
        (new BukkitRunnable() {
            public void run() {
                for(Player viewer : PacketManager.this.plugin.getServer().getOnlinePlayers()) {
                    for(Player target : PacketManager.this.plugin.getServer().getOnlinePlayers()) {
                        if (!viewer.equals(target)) {
                            UUID viewerId = viewer.getUniqueId();
                            UUID targetId = target.getUniqueId();
                            boolean shouldBeHidden = PacketManager.this.isPlayerHidden(viewerId, targetId);
                            boolean isCurrentlyHidden = !viewer.canSee(target);
                            if (shouldBeHidden && !isCurrentlyHidden) {
                                PacketManager.this.hidePlayer(viewerId, targetId);
                            } else if (!shouldBeHidden && isCurrentlyHidden) {
                                PacketManager.this.showPlayer(viewerId, targetId);
                            }
                        }
                    }
                }

            }
        }).runTask(this.plugin);
    }

    @EventHandler(
        priority = EventPriority.MONITOR
    )
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        this.clearPlayerData(playerId);
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("清理玩家 " + player.getName() + " 的数据包管理器数据");
        }

    }

    public void cleanup() {
        for(Player viewer : this.plugin.getServer().getOnlinePlayers()) {
            for(Player target : this.plugin.getServer().getOnlinePlayers()) {
                if (this.isPlayerHidden(viewer.getUniqueId(), target.getUniqueId())) {
                    this.showPlayer(viewer.getUniqueId(), target.getUniqueId());
                }
            }
        }

        this.hiddenPlayers.clear();
        this.visiblePlayers.clear();
        this.plugin.getLogger().info("数据包管理器已清理");
    }

    public void reloadConfig() {
        this.plugin.getLogger().info("数据包管理器配置已重新加载");
    }
}
