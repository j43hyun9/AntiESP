//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.utils;

import com.Antiesp.AntiEsp.AntiEsp;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamUtils {
    private final AntiEsp plugin;

    public TeamUtils(AntiEsp plugin) {
        this.plugin = plugin;
    }

    public boolean arePlayersInSameTeam(Player player1, Player player2) {
        if (player1 != null && player2 != null) {
            if (player1.equals(player2)) {
                return false;
            } else {
                String detectionMethod = this.plugin.getTeamDetectionMethod();
                switch (detectionMethod.toLowerCase()) {
                    case "scoreboard":
                        return this.arePlayersInSameScoreboardTeam(player1, player2);
                    case "permission":
                        return this.arePlayersInSamePermissionTeam(player1, player2);
                    case "both":
                        return this.arePlayersInSameScoreboardTeam(player1, player2) || this.arePlayersInSamePermissionTeam(player1, player2);
                    default:
                        this.plugin.getLogger().warning("未知的队伍检测方法: " + detectionMethod + "，使用默认的计分板检测");
                        return this.arePlayersInSameScoreboardTeam(player1, player2);
                }
            }
        } else {
            return false;
        }
    }

    private boolean arePlayersInSameScoreboardTeam(Player player1, Player player2) {
        try {
            Scoreboard scoreboard = player1.getScoreboard();
            if (scoreboard == null) {
                return false;
            } else {
                Team team1 = scoreboard.getPlayerTeam(player1);
                Team team2 = scoreboard.getPlayerTeam(player2);
                if (team1 != null && team2 != null) {
                    boolean sameTeam = team1.equals(team2);
                    if (this.plugin.isDebugMode()) {
                        Logger var10000 = this.plugin.getLogger();
                        String var10001 = player1.getName();
                        var10000.info("计分板队伍检测: " + var10001 + " (队伍: " + (team1 != null ? team1.getName() : "无") + ") vs " + player2.getName() + " (队伍: " + (team2 != null ? team2.getName() : "无") + ") = " + sameTeam);
                    }

                    return sameTeam;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("计分板队伍检测时发生错误: " + e.getMessage());
            }

            return false;
        }
    }

    private boolean arePlayersInSamePermissionTeam(Player player1, Player player2) {
        try {
            String team1 = this.getPlayerTeamFromPermissions(player1);
            String team2 = this.getPlayerTeamFromPermissions(player2);
            if (team1 != null && team2 != null && !team1.isEmpty() && !team2.isEmpty()) {
                boolean sameTeam = team1.equals(team2);
                if (this.plugin.isDebugMode()) {
                    Logger var10000 = this.plugin.getLogger();
                    String var10001 = player1.getName();
                    var10000.info("权限队伍检测: " + var10001 + " (队伍: " + team1 + ") vs " + player2.getName() + " (队伍: " + team2 + ") = " + sameTeam);
                }

                return sameTeam;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().warning("权限队伍检测时发生错误: " + e.getMessage());
            }

            return false;
        }
    }

    private String getPlayerTeamFromPermissions(Player player) {
        for(PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission();
            if (perm.startsWith("antiesp.team.")) {
                return perm.substring("antiesp.team.".length());
            }
        }

        return null;
    }

    public boolean hasTeamExemptionPermission(Player player) {
        return player.hasPermission("antiesp.team.exemption");
    }

    public String getPlayerTeamInfo(Player player) {
        if (player == null) {
            return "null";
        } else {
            StringBuilder info = new StringBuilder();
            info.append("玩家: ").append(player.getName());

            try {
                Scoreboard scoreboard = player.getScoreboard();
                if (scoreboard != null) {
                    Team team = scoreboard.getPlayerTeam(player);
                    info.append(" | 计分板队伍: ").append(team != null ? team.getName() : "无");
                }
            } catch (Exception var5) {
                info.append(" | 计分板队伍: 错误");
            }

            String permTeam = this.getPlayerTeamFromPermissions(player);
            info.append(" | 权限队伍: ").append(permTeam != null ? permTeam : "无");
            return info.toString();
        }
    }

    public boolean shouldApplyTeamExemption(Player viewer, Player target) {
        if (!this.plugin.isTeamExemptionEnabled()) {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().info("队伍豁免已禁用");
            }

            return false;
        } else if (!viewer.hasPermission("antiesp.bypass") && !target.hasPermission("antiesp.bypass")) {
            boolean sameTeam = this.arePlayersInSameTeam(viewer, target);
            if (this.plugin.isDebugMode()) {
                Logger var10000 = this.plugin.getLogger();
                String var10001 = viewer.getName();
                var10000.info("队伍豁免检查: " + var10001 + " vs " + target.getName() + " | 同队: " + sameTeam + " | 豁免生效: " + sameTeam);
            }

            return sameTeam;
        } else {
            if (this.plugin.isDebugMode()) {
                this.plugin.getLogger().info("玩家有绕过权限，不应用队伍豁免");
            }

            return false;
        }
    }
}
