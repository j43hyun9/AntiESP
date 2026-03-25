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
            this.plugin.getLogger().info("=== 가시성 업데이트 시작 ===");
            this.plugin.getLogger().info("온라인 플레이어 수: " + onlinePlayers.size());
            this.plugin.getLogger().info("처리 시간: " + System.currentTimeMillis());
        }

        long startTime = System.nanoTime();
        int processedPlayers = 0;

        for(Player player : onlinePlayers) {
            if (this.processingPlayers.contains(player.getUniqueId())) {
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().info("처리 중인 플레이어 건너뜀: " + player.getName());
                }
            } else {
                this.updatePlayerVisibility(player);
                ++processedPlayers;
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000L;
        if (this.plugin.isDebugMode()) {
            this.plugin.getLogger().info("=== 가시성 업데이트 완료 ===");
            this.plugin.getLogger().info("처리된 플레이어 수: " + processedPlayers);
            this.plugin.getLogger().info("총 소요 시간: " + duration + "ms");
            this.plugin.getLogger().info("평균 소요 시간: " + (processedPlayers > 0 ? duration / (long)processedPlayers : 0L) + "ms/플레이어");
        }

    }

    private void updatePlayerVisibility(Player viewer) {
        UUID viewerId = viewer.getUniqueId();
        this.processingPlayers.add(viewerId);
        if (this.plugin.isDebugMode()) {
            Logger var10000 = this.plugin.getLogger();
            String var10001 = viewer.getName();
            var10000.info("플레이어 처리 시작: " + var10001 + " (" + String.valueOf(viewerId) + ")");
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
                    var31.info("관찰자 위치: " + var44);
                    this.plugin.getLogger().info("이전 가시 플레이어 수: " + lastVisible.size());
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
                                this.plugin.getLogger().info("대상 " + target.getName() + " 이(가) 다른 세계에 있음, 건너뜀");
                            }
                        } else {
                            double distance = viewerLocation.distance(targetLocation);
                            if (distance > this.plugin.getMaxDistance()) {
                                if (this.plugin.isDebugMode()) {
                                    Logger var32 = this.plugin.getLogger();
                                    String var45 = target.getName();
                                    var32.info("대상 " + var45 + " 이(가) 너무 멀리 있음 (" + String.format("%.2f", distance) + " > " + this.plugin.getMaxDistance() + "), 건너뜀");
                                }
                            } else {
                                if (this.plugin.isDebugMode()) {
                                    Logger var33 = this.plugin.getLogger();
                                    String var46 = target.getName();
                                    var33.info("대상 확인: " + var46 + " 거리: " + String.format("%.2f", distance));
                                }

                                boolean teamExemption = this.teamUtils.shouldApplyTeamExemption(viewer, target);
                                if (this.plugin.isDebugMode()) {
                                    Logger var34 = this.plugin.getLogger();
                                    String var47 = viewer.getName();
                                    var34.info("팀 면제 확인 결과: " + var47 + " vs " + target.getName() + " = " + teamExemption);
                                }

                                if (teamExemption) {
                                    if (this.plugin.isDebugMode()) {
                                        Logger var35 = this.plugin.getLogger();
                                        String var48 = viewer.getName();
                                        var35.info("팀 면제 적용: " + var48 + " 과(와) " + target.getName() + " 이(가) 같은 팀, 반투시 건너뜀");
                                    }

                                    currentVisible.add(targetId);
                                    ++visibleTargets;
                                    if (!lastVisible.contains(targetId)) {
                                        this.plugin.getPacketManager().showPlayer(viewerId, targetId);
                                        ++stateChanges;
                                        if (this.plugin.isDebugMode()) {
                                            Logger var36 = this.plugin.getLogger();
                                            String var49 = viewer.getName();
                                            var36.info("팀 면제 표시: " + var49 + " 이(가) 이제 " + target.getName() + " 을(를) 볼 수 있음 (팀 면제)");
                                        }
                                    } else if (this.plugin.isDebugMode()) {
                                        Logger var37 = this.plugin.getLogger();
                                        String var50 = viewer.getName();
                                        var37.info("팀 면제 유지: " + var50 + " 이(가) 계속 " + target.getName() + " 을(를) 볼 수 있음 (팀 면제)");
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
                                                var38.info("상태 변화: " + var51 + " 이(가) 이제 " + target.getName() + " 을(를) 볼 수 있음 (표시)");
                                            }
                                        } else if (this.plugin.isDebugMode()) {
                                            Logger var39 = this.plugin.getLogger();
                                            String var52 = viewer.getName();
                                            var39.info("상태 유지: " + var52 + " 이(가) 계속 " + target.getName() + " 을(를) 볼 수 있음");
                                        }
                                    } else {
                                        ++hiddenTargets;
                                        if (lastVisible.contains(targetId)) {
                                            this.plugin.getPacketManager().hidePlayer(viewerId, targetId);
                                            ++stateChanges;
                                            if (this.plugin.isDebugMode()) {
                                                Logger var40 = this.plugin.getLogger();
                                                String var53 = viewer.getName();
                                                var40.info("상태 변화: " + var53 + " 이(가) 이제 " + target.getName() + " 을(를) 볼 수 없음 (숨김)");
                                            }
                                        } else if (this.plugin.isDebugMode()) {
                                            Logger var41 = this.plugin.getLogger();
                                            String var54 = viewer.getName();
                                            var41.info("상태 유지: " + var54 + " 이(가) 계속 " + target.getName() + " 을(를) 볼 수 없음");
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
                                var42.info("정리 숨김: " + var55 + " 이(가) " + targetPlayer.getName() + " 을(를) 숨김 (정리)");
                            }
                        }
                    }
                }

                this.lastVisiblePlayers.put(viewerId, currentVisible);
                this.lastPlayerLocations.put(viewerId, viewerLocation.clone());
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000L;
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().info("=== 플레이어 " + viewer.getName() + " 처리 완료 ===");
                    this.plugin.getLogger().info("확인된 대상 수: " + checkedTargets);
                    this.plugin.getLogger().info("가시 대상 수: " + visibleTargets);
                    this.plugin.getLogger().info("숨겨진 대상 수: " + hiddenTargets);
                    this.plugin.getLogger().info("상태 변화 횟수: " + stateChanges);
                    this.plugin.getLogger().info("처리 소요 시간: " + duration + "ms");
                    this.plugin.getLogger().info("현재 가시 플레이어: " + currentVisible.size());
                }

                return;
            }

            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().info("플레이어 " + viewer.getName() + " 이(가) 우회 권한을 가지고 있음, 처리 건너뜀");
            }
        } catch (Exception e) {
            Logger var30 = this.plugin.getLogger();
            String var43 = viewer.getName();
            var30.warning("플레이어 " + var43 + " 의 가시성 업데이트 중 오류 발생: " + e.getMessage());
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
                    var10000.info("눈 높이 감지 사용: 관찰자 " + var10001 + " -> 대상 " + this.formatLocation(targetEyeLoc));
                }
            } else {
                viewerEyeLoc = viewerLoc.clone();
                targetEyeLoc = targetLoc.clone();
                if (this.plugin.isDebugMode()) {
                    Logger var18 = this.plugin.getLogger();
                    String var22 = this.formatLocation(viewerEyeLoc);
                    var18.info("발 높이 감지 사용: 관찰자 " + var22 + " -> 대상 " + this.formatLocation(targetEyeLoc));
                }
            }

            double distance = viewerEyeLoc.distance(targetEyeLoc);
            if (distance > this.plugin.getMaxDistance()) {
                if (this.plugin.isDebugMode()) {
                    Logger var21 = this.plugin.getLogger();
                    String var25 = String.format("%.2f", distance);
                    var21.info("거리 초과: " + var25 + " > " + this.plugin.getMaxDistance() + " (보이지 않음)");
                }

                return false;
            } else if (distance <= this.plugin.getMinDistance()) {
                if (this.plugin.isDebugMode()) {
                    Logger var20 = this.plugin.getLogger();
                    String var24 = String.format("%.2f", distance);
                    var20.info("근거리 면제: " + var24 + " <= " + this.plugin.getMinDistance() + " (보임)");
                }

                return true;
            } else {
                boolean canSee;
                String detectionMethod;
                if (this.plugin.isStrictModeEnabled()) {
                    if (this.plugin.isMultiAngleCheckEnabled()) {
                        canSee = RaycastUtils.hasLineOfSightMultiAngle(viewerEyeLoc, targetEyeLoc, this.plugin.getMaxDistance(), this.plugin.getBlockingThreshold(), this.plugin.getMultiAngleRadius());
                        detectionMethod = "다각도 감지 (반경: " + this.plugin.getMultiAngleRadius() + ")";
                    } else {
                        canSee = RaycastUtils.hasLineOfSightSmart(viewerEyeLoc, targetEyeLoc, this.plugin.getMaxDistance(), this.plugin.getBlockingThreshold());
                        detectionMethod = "스마트 감지";
                    }
                } else {
                    canSee = RaycastUtils.hasLineOfSight(viewerEyeLoc, targetEyeLoc, this.plugin.getMaxDistance());
                    detectionMethod = "기본 감지";
                }

                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000L;
                if (this.plugin.isDebugMode()) {
                    this.plugin.getLogger().info("시선 감지 결과: " + (canSee ? "보임" : "보이지 않음") + " | 방법: " + detectionMethod + " | 거리: " + String.format("%.2f", distance) + " | 소요 시간: " + duration + "μs");
                    if (this.plugin.isStrictModeEnabled()) {
                        Logger var19 = this.plugin.getLogger();
                        double var23 = this.plugin.getBlockingThreshold();
                        var19.info("엄격 모드: 활성화 | 차단 임계값: " + var23 * (double)100.0F + "%");
                    }
                }

                return canSee;
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("시선 감지 중 오류 발생: " + e.getMessage());
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
            this.plugin.getLogger().info("플레이어 " + player.getName() + " 의 가시성 데이터 초기화됨");
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
        this.plugin.getLogger().info("시선 매니저가 정리되었습니다");
    }

    public void reloadConfig() {
        this.plugin.getLogger().info("시선 매니저 설정이 다시 로드되었습니다");
    }
}
