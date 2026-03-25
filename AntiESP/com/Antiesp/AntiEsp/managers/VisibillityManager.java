//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.managers;

import com.Antiesp.AntiEsp.AntiEsp;
import com.Antiesp.AntiEsp.utils.RaycastUtils;
import com.Antiesp.AntiEsp.utils.TeamUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VisibilityManager {
    private final AntiEsp plugin;
    private final Map<UUID, Set<UUID>> lastVisiblePlayers;
    private final Map<UUID, Location> lastPlayerLocations;
    private final Set<UUID> processingPlayers;
    private final TeamUtils teamUtils;

    public VisibilityManager(AntiEsp plugin) {
        this.plugin = plugin;
        this.lastVisiblePlayers = new ConcurrentHashMap();
        this.lastPlayerLocations = new ConcurrentHashMap();
        this.processingPlayers = ConcurrentHashMap.newKeySet();
        this.teamUtils = new TeamUtils(plugin);
    }

    public void updateVisibility() {
        List<Player> onlinePlayers = new ArrayList(this.plugin.getServer().getOnlinePlayers());
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("=== 开始可见性更新 ===");
            this.plugin.getLogger().info("在线玩家数量: " + onlinePlayers.size());
            this.plugin.getLogger().info("处理时间: " + System.currentTimeMillis());
        }

        long startTime = System.nanoTime();
        int processedPlayers = 0;

        for(Player player : onlinePlayers) {
            if (this.processingPlayers.contains(player.getUniqueId())) {
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().info("跳过正在处理的玩家: " + player.getName());
                }
            } else {
                this.updatePlayerVisibility(player);
                ++processedPlayers;
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000L;
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("=== 可见性更新完成 ===");
            this.plugin.getLogger().info("处理玩家数量: " + processedPlayers);
            this.plugin.getLogger().info("总耗时: " + duration + "ms");
            this.plugin.getLogger().info("平均耗时: " + (processedPlayers > 0 ? duration / (long)processedPlayers : 0L) + "ms/玩家");
        }

    }

    private void updatePlayerVisibility(Player viewer) {
        UUID viewerId = viewer.getUniqueId();
        this.processingPlayers.add(viewerId);
        if (this.plugin.isDebugMode()) {
            Logger var10000 = this.plugin.getLogger();
            String var10001 = viewer.getName();
            var10000.info("开始处理玩家: " + var10001 + " (" + String.valueOf(viewerId) + ")");
        }

        long startTime = System.nanoTime();

        try {
            if (!viewer.hasPermission("antiesp.bypass")) {
                Location viewerLocation = viewer.getLocation();
                Set<UUID> currentVisible = ConcurrentHashMap.newKeySet();
                Set<UUID> lastVisible = (Set)this.lastVisiblePlayers.getOrDefault(viewerId, ConcurrentHashMap.newKeySet());
                if (this.plugin.isDebugMode()) {
                    Logger var31 = this.plugin.getLogger();
                    String var44 = this.formatLocation(viewerLocation);
                    var31.info("观察者位置: " + var44);
                    this.plugin.getLogger().info("上次可见玩家数量: " + lastVisible.size());
                }

                int checkedTargets = 0;
                int visibleTargets = 0;
                int hiddenTargets = 0;
                int stateChanges = 0;

                for(Player target : this.plugin.getServer().getOnlinePlayers()) {
                    if (!target.equals(viewer) && !target.hasPermission("antiesp.bypass")) {
                        ++checkedTargets;
                        UUID targetId = target.getUniqueId();
                        Location targetLocation = target.getLocation();
                        if (viewerLocation.getWorld() != targetLocation.getWorld()) {
                            if (this.plugin.isDebugMode()) {
                                this.plugin.getLogger().info("目标 " + target.getName() + " 在不同世界，跳过");
                            }
                        } else {
                            double distance = viewerLocation.distance(targetLocation);
                            if (distance > this.plugin.getMaxDistance()) {
                                if (this.plugin.isDebugMode()) {
                                    Logger var32 = this.plugin.getLogger();
                                    String var45 = target.getName();
                                    var32.info("目标 " + var45 + " 距离过远 (" + String.format("%.2f", distance) + " > " + this.plugin.getMaxDistance() + ")，跳过");
                                }
                            } else {
                                if (this.plugin.isDebugMode()) {
                                    Logger var33 = this.plugin.getLogger();
                                    String var46 = target.getName();
                                    var33.info("检查目标: " + var46 + " 距离: " + String.format("%.2f", distance));
                                }

                                boolean teamExemption = this.teamUtils.shouldApplyTeamExemption(viewer, target);
                                if (this.plugin.isDebugMode()) {
                                    Logger var34 = this.plugin.getLogger();
                                    String var47 = viewer.getName();
                                    var34.info("队伍豁免检查结果: " + var47 + " vs " + target.getName() + " = " + teamExemption);
                                }

                                if (teamExemption) {
                                    if (this.plugin.isDebugMode()) {
                                        Logger var35 = this.plugin.getLogger();
                                        String var48 = viewer.getName();
                                        var35.info("队伍豁免生效: " + var48 + " 和 " + target.getName() + " 在同一队伍，跳过反透视");
                                    }

                                    currentVisible.add(targetId);
                                    ++visibleTargets;
                                    if (!lastVisible.contains(targetId)) {
                                        this.plugin.getPacketManager().showPlayer(viewerId, targetId);
                                        ++stateChanges;
                                        if (this.plugin.isDebugMode()) {
                                            Logger var36 = this.plugin.getLogger();
                                            String var49 = viewer.getName();
                                            var36.info("队伍豁免显示: " + var49 + " 现在可以看到 " + target.getName() + " (队伍豁免)");
                                        }
                                    } else if (this.plugin.isDebugMode()) {
                                        Logger var37 = this.plugin.getLogger();
                                        String var50 = viewer.getName();
                                        var37.info("队伍豁免保持: " + var50 + " 继续可以看到 " + target.getName() + " (队伍豁免)");
                                    }
                                } else {
                                    boolean canSee = this.canPlayerSee(viewer, target, viewerLocation, targetLocation);
                                    if (canSee) {
                                        currentVisible.add(targetId);
                                        ++visibleTargets;
                                        if (!lastVisible.contains(targetId)) {
                                            this.plugin.getPacketManager().showPlayer(viewerId, targetId);
                                            ++stateChanges;
                                            if (this.plugin.isDebugMode()) {
                                                Logger var38 = this.plugin.getLogger();
                                                String var51 = viewer.getName();
                                                var38.info("状态变化: " + var51 + " 现在可以看到 " + target.getName() + " (显示)");
                                            }
                                        } else if (this.plugin.isDebugMode()) {
                                            Logger var39 = this.plugin.getLogger();
                                            String var52 = viewer.getName();
                                            var39.info("状态保持: " + var52 + " 继续可以看到 " + target.getName());
                                        }
                                    } else {
                                        ++hiddenTargets;
                                        if (lastVisible.contains(targetId)) {
                                            this.plugin.getPacketManager().hidePlayer(viewerId, targetId);
                                            ++stateChanges;
                                            if (this.plugin.isDebugMode()) {
                                                Logger var40 = this.plugin.getLogger();
                                                String var53 = viewer.getName();
                                                var40.info("状态变化: " + var53 + " 现在看不到 " + target.getName() + " (隐藏)");
                                            }
                                        } else if (this.plugin.isDebugMode()) {
                                            Logger var41 = this.plugin.getLogger();
                                            String var54 = viewer.getName();
                                            var41.info("状态保持: " + var54 + " 继续看不到 " + target.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for(UUID targetId : lastVisible) {
                    if (!currentVisible.contains(targetId)) {
                        this.plugin.getPacketManager().hidePlayer(viewerId, targetId);
                        ++stateChanges;
                        if (this.plugin.isDebugMode()) {
                            Player targetPlayer = this.plugin.getServer().getPlayer(targetId);
                            if (targetPlayer != null) {
                                Logger var42 = this.plugin.getLogger();
                                String var55 = viewer.getName();
                                var42.info("清理隐藏: " + var55 + " 隐藏 " + targetPlayer.getName() + " (清理)");
                            }
                        }
                    }
                }

                this.lastVisiblePlayers.put(viewerId, currentVisible);
                this.lastPlayerLocations.put(viewerId, viewerLocation.clone());
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000L;
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().info("=== 玩家 " + viewer.getName() + " 处理完成 ===");
                    this.plugin.getLogger().info("检查目标数量: " + checkedTargets);
                    this.plugin.getLogger().info("可见目标数量: " + visibleTargets);
                    this.plugin.getLogger().info("隐藏目标数量: " + hiddenTargets);
                    this.plugin.getLogger().info("状态变化次数: " + stateChanges);
                    this.plugin.getLogger().info("处理耗时: " + duration + "ms");
                    this.plugin.getLogger().info("当前可见玩家: " + currentVisible.size());
                }

                return;
            }

            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().info("玩家 " + viewer.getName() + " 有绕过权限，跳过处理");
            }
        } catch (Exception e) {
            Logger var30 = this.plugin.getLogger();
            String var43 = viewer.getName();
            var30.warning("更新玩家 " + var43 + " 的可见性时发生错误: " + e.getMessage());
            if (this.plugin.isDebugMode()) {
                e.printStackTrace();
            }

            return;
        } finally {
            this.processingPlayers.remove(viewerId);
        }

    }

    private boolean canPlayerSee(Player viewer, Player target, Location viewerLoc, Location targetLoc) {
        long startTime = System.nanoTime();

        try {
            Location viewerEyeLoc;
            Location targetEyeLoc;
            if (this.plugin.isUseEyeHeight()) {
                viewerEyeLoc = viewerLoc.clone().add((double)0.0F, 1.6, (double)0.0F);
                targetEyeLoc = targetLoc.clone().add((double)0.0F, 1.6, (double)0.0F);
                if (this.plugin.isDebugMode()) {
                    Logger var10000 = this.plugin.getLogger();
                    String var10001 = this.formatLocation(viewerEyeLoc);
                    var10000.info("使用眼睛高度检测: 观察者 " + var10001 + " -> 目标 " + this.formatLocation(targetEyeLoc));
                }
            } else {
                viewerEyeLoc = viewerLoc.clone();
                targetEyeLoc = targetLoc.clone();
                if (this.plugin.isDebugMode()) {
                    Logger var18 = this.plugin.getLogger();
                    String var22 = this.formatLocation(viewerEyeLoc);
                    var18.info("使用脚部高度检测: 观察者 " + var22 + " -> 目标 " + this.formatLocation(targetEyeLoc));
                }
            }

            double distance = viewerEyeLoc.distance(targetEyeLoc);
            if (distance > this.plugin.getMaxDistance()) {
                if (this.plugin.isDebugMode()) {
                    Logger var21 = this.plugin.getLogger();
                    String var25 = String.format("%.2f", distance);
                    var21.info("距离过远: " + var25 + " > " + this.plugin.getMaxDistance() + " (不可见)");
                }

                return false;
            } else if (distance <= this.plugin.getMinDistance()) {
                if (this.plugin.isDebugMode()) {
                    Logger var20 = this.plugin.getLogger();
                    String var24 = String.format("%.2f", distance);
                    var20.info("近距离豁免: " + var24 + " <= " + this.plugin.getMinDistance() + " (可见)");
                }

                return true;
            } else {
                boolean canSee;
                String detectionMethod;
                if (this.plugin.isStrictModeEnabled()) {
                    if (this.plugin.isMultiAngleCheckEnabled()) {
                        canSee = RaycastUtils.hasLineOfSightMultiAngle(viewerEyeLoc, targetEyeLoc, this.plugin.getMaxDistance(), this.plugin.getBlockingThreshold(), this.plugin.getMultiAngleRadius());
                        detectionMethod = "多角度检测 (半径: " + this.plugin.getMultiAngleRadius() + ")";
                    } else {
                        canSee = RaycastUtils.hasLineOfSightSmart(viewerEyeLoc, targetEyeLoc, this.plugin.getMaxDistance(), this.plugin.getBlockingThreshold());
                        detectionMethod = "智能检测";
                    }
                } else {
                    canSee = RaycastUtils.hasLineOfSight(viewerEyeLoc, targetEyeLoc, this.plugin.getMaxDistance());
                    detectionMethod = "基础检测";
                }

                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000L;
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().info("视线检测结果: " + (canSee ? "可见" : "不可见") + " | 方法: " + detectionMethod + " | 距离: " + String.format("%.2f", distance) + " | 耗时: " + duration + "μs");
                    if (this.plugin.isStrictModeEnabled()) {
                        Logger var19 = this.plugin.getLogger();
                        double var23 = this.plugin.getBlockingThreshold();
                        var19.info("严格模式: 启用 | 阻挡阈值: " + var23 * (double)100.0F + "%");
                    }
                }

                return canSee;
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("视线检测时发生错误: " + e.getMessage());
                e.printStackTrace();
            }

            return false;
        }
    }

    public void forceUpdatePlayer(Player player) {
        if (player != null && player.isOnline()) {
            this.updatePlayerVisibility(player);
        }
    }

    public int getVisiblePlayerCount(Player player) {
        return ((Set)this.lastVisiblePlayers.getOrDefault(player.getUniqueId(), ConcurrentHashMap.newKeySet())).size();
    }

    public int getHiddenPlayerCount(Player player) {
        return this.plugin.getPacketManager().getHiddenPlayerCount(player.getUniqueId());
    }

    public void clearPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        this.lastVisiblePlayers.remove(playerId);
        this.lastPlayerLocations.remove(playerId);
        this.processingPlayers.remove(playerId);
        this.plugin.getPacketManager().clearPlayerData(playerId);
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("已清除玩家 " + player.getName() + " 的可见性数据");
        }

    }

    public int getProcessingPlayerCount() {
        return this.processingPlayers.size();
    }

    public boolean isPlayerProcessing(Player player) {
        return this.processingPlayers.contains(player.getUniqueId());
    }

    private String formatLocation(Location location) {
        return location == null ? "null" : String.format("(%s, %.2f, %.2f, %.2f)", location.getWorld() != null ? location.getWorld().getName() : "null", location.getX(), location.getY(), location.getZ());
    }

    public void cleanup() {
        this.lastVisiblePlayers.clear();
        this.lastPlayerLocations.clear();
        this.processingPlayers.clear();
        this.plugin.getLogger().info("视线管理器已清理");
    }

    public void reloadConfig() {
        this.plugin.getLogger().info("视线管理器配置已重新加载");
    }
}
