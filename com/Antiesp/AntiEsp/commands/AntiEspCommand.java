//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.commands;

import com.Antiesp.AntiEsp.AntiEsp;
import com.Antiesp.AntiEsp.managers.LanguageManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class AntiEspCommand implements CommandExecutor, TabCompleter {
    private final AntiEsp plugin;

    public AntiEspCommand(AntiEsp plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("antiesp.admin")) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "이 명령어를 사용할 권한이 없습니다!");
            return true;
        } else if (args.length == 0) {
            this.sendHelpMessage(sender);
            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "reload":
                    this.handleReload(sender);
                    break;
                case "status":
                    this.handleStatus(sender);
                    break;
                case "debug":
                    this.handleDebug(sender, args);
                    break;
                case "info":
                    this.handleInfo(sender);
                    break;
                case "stats":
                    this.handleStats(sender);
                    break;
                case "strict":
                    this.handleStrict(sender, args);
                    break;
                case "lang":
                case "language":
                    this.handleLanguage(sender, args);
                    break;
                case "team":
                    this.handleTeam(sender, args);
                    break;
                default:
                    this.sendHelpMessage(sender);
            }

            return true;
        }
    }

    private void handleReload(CommandSender sender) {
        try {
            this.plugin.reloadConfiguration();
            this.plugin.getLanguageManager().reloadLanguages();
            if (sender instanceof Player) {
                this.plugin.getLanguageManager().sendMessage((Player)sender, "commands.reload.success");
            } else {
                String successMessage = this.plugin.getLanguageManager().getMessage("commands.reload.success", "ko");
                sender.sendMessage(successMessage);
            }
        } catch (Exception var4) {
            if (sender instanceof Player) {
                String errorMessage = this.plugin.getLanguageManager().getMessage("commands.reload.failed", Map.of("error", var4.getMessage()));
                sender.sendMessage(errorMessage);
            } else {
                String errorMessage = this.plugin.getLanguageManager().getMessage("commands.reload.failed", "ko", Map.of("error", var4.getMessage()));
                sender.sendMessage(errorMessage);
            }

            this.plugin.getLogger().warning("설정 리로드 중 오류 발생: " + var4.getMessage());
        }

    }

    private void handleStatus(CommandSender sender) {
        if (sender instanceof Player player) {
            LanguageManager lang = this.plugin.getLanguageManager();
            player.sendMessage(lang.getMessage("status.header"));
            player.sendMessage(lang.getMessage("status.version", Map.of("version", this.plugin.getDescription().getVersion())));
            player.sendMessage(lang.getMessage("status.author", Map.of("author", String.join(", ", this.plugin.getDescription().getAuthors()))));
            player.sendMessage(lang.getMessage("status.debug_mode", Map.of("status", this.plugin.isDebugMode() ? lang.getMessage("common.enabled") : lang.getMessage("common.disabled"))));
            player.sendMessage(lang.getMessage("status.check_interval", Map.of("interval", String.valueOf(this.plugin.getCheckInterval()))));
            player.sendMessage(lang.getMessage("status.max_distance", Map.of("distance", String.valueOf(this.plugin.getMaxDistance()))));
            player.sendMessage(lang.getMessage("status.min_distance", Map.of("distance", String.valueOf(this.plugin.getMinDistance()))));
            player.sendMessage(lang.getMessage("status.packet_interception", Map.of("status", this.plugin.isPacketInterceptionEnabled() ? lang.getMessage("common.enabled") : lang.getMessage("common.disabled"))));
            player.sendMessage(lang.getMessage("status.online_players", Map.of("count", String.valueOf(this.plugin.getServer().getOnlinePlayers().size()))));
        } else {
            LanguageManager lang = this.plugin.getLanguageManager();
            sender.sendMessage(lang.getMessage("status.header", "ko"));
            sender.sendMessage(lang.getMessage("status.version", "ko", Map.of("version", this.plugin.getDescription().getVersion())));
            sender.sendMessage(lang.getMessage("status.author", "ko", Map.of("author", String.join(", ", this.plugin.getDescription().getAuthors()))));
            sender.sendMessage(lang.getMessage("status.debug_mode", "ko", Map.of("status", this.plugin.isDebugMode() ? lang.getMessage("common.enabled", "ko") : lang.getMessage("common.disabled", "ko"))));
            sender.sendMessage(lang.getMessage("status.check_interval", "ko", Map.of("interval", String.valueOf(this.plugin.getCheckInterval()))));
            sender.sendMessage(lang.getMessage("status.max_distance", "ko", Map.of("distance", String.valueOf(this.plugin.getMaxDistance()))));
            sender.sendMessage(lang.getMessage("status.min_distance", "ko", Map.of("distance", String.valueOf(this.plugin.getMinDistance()))));
            sender.sendMessage(lang.getMessage("status.packet_interception", "ko", Map.of("status", this.plugin.isPacketInterceptionEnabled() ? lang.getMessage("common.enabled", "ko") : lang.getMessage("common.disabled", "ko"))));
            sender.sendMessage(lang.getMessage("status.online_players", "ko", Map.of("count", String.valueOf(this.plugin.getServer().getOnlinePlayers().size()))));
        }

    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player) {
                this.plugin.getLanguageManager().sendMessage((Player)sender, "commands.debug.usage");
            } else {
                String usageMessage = this.plugin.getLanguageManager().getMessage("commands.debug.usage", "ko");
                sender.sendMessage(usageMessage);
            }

        } else {
            String action = args[1].toLowerCase();
            boolean newDebugMode;
            if (!action.equals("on") && !action.equals("true") && !action.equals("enable")) {
                if (!action.equals("off") && !action.equals("false") && !action.equals("disable")) {
                    if (sender instanceof Player) {
                        this.plugin.getLanguageManager().sendMessage((Player)sender, "commands.debug.usage");
                    } else {
                        String usageMessage = this.plugin.getLanguageManager().getMessage("commands.debug.usage", "ko");
                        sender.sendMessage(usageMessage);
                    }

                    return;
                }

                newDebugMode = false;
            } else {
                newDebugMode = true;
            }

            this.plugin.getConfig().set("debug", newDebugMode);
            this.plugin.saveConfig();
            this.plugin.reloadConfiguration();
            if (sender instanceof Player) {
                Player player = (Player)sender;
                LanguageManager lang = this.plugin.getLanguageManager();
                if (newDebugMode) {
                    lang.sendMessage(player, "commands.debug.enabled");
                } else {
                    lang.sendMessage(player, "commands.debug.disabled");
                }
            } else {
                LanguageManager lang = this.plugin.getLanguageManager();
                if (newDebugMode) {
                    String enabledMessage = lang.getMessage("commands.debug.enabled", "ko");
                    sender.sendMessage(enabledMessage);
                } else {
                    String disabledMessage = lang.getMessage("commands.debug.disabled", "ko");
                    sender.sendMessage(disabledMessage);
                }
            }

        }
    }

    private void handleInfo(CommandSender sender) {
        if (sender instanceof Player player) {
            LanguageManager lang = this.plugin.getLanguageManager();
            player.sendMessage(lang.getMessage("info.header"));
            player.sendMessage(lang.getMessage("info.description"));
            player.sendMessage(lang.getMessage("info.description2"));
            player.sendMessage(lang.getMessage("info.description3"));
            player.sendMessage("");
            player.sendMessage(lang.getMessage("info.features.header"));
            player.sendMessage(lang.getMessage("info.features.real_time"));
            player.sendMessage(lang.getMessage("info.features.packet_interception"));
            player.sendMessage(lang.getMessage("info.features.player_hiding"));
            player.sendMessage(lang.getMessage("info.features.performance_optimization"));
            player.sendMessage(lang.getMessage("info.features.close_distance_exemption"));
            player.sendMessage(lang.getMessage("info.features.multi_angle_detection"));
            player.sendMessage(lang.getMessage("info.features.eye_height_detection"));
        } else {
            LanguageManager lang = this.plugin.getLanguageManager();
            sender.sendMessage(lang.getMessage("info.header", "ko"));
            sender.sendMessage(lang.getMessage("info.description", "ko"));
            sender.sendMessage(lang.getMessage("info.description2", "ko"));
            sender.sendMessage(lang.getMessage("info.description3", "ko"));
            sender.sendMessage("");
            sender.sendMessage(lang.getMessage("info.features.header", "ko"));
            sender.sendMessage(lang.getMessage("info.features.real_time", "ko"));
            sender.sendMessage(lang.getMessage("info.features.packet_interception", "ko"));
            sender.sendMessage(lang.getMessage("info.features.player_hiding", "ko"));
            sender.sendMessage(lang.getMessage("info.features.performance_optimization", "ko"));
            sender.sendMessage(lang.getMessage("info.features.close_distance_exemption", "ko"));
            sender.sendMessage(lang.getMessage("info.features.multi_angle_detection", "ko"));
            sender.sendMessage(lang.getMessage("info.features.eye_height_detection", "ko"));
        }

    }

    private void handleStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            String playerOnlyMessage = this.plugin.getLanguageManager().getMessage("commands.player_only", "ko");
            sender.sendMessage(playerOnlyMessage);
        } else {
            LanguageManager lang = this.plugin.getLanguageManager();
            player.sendMessage(lang.getMessage("stats.header"));
            player.sendMessage(lang.getMessage("stats.visible_players", Map.of("count", String.valueOf(this.plugin.getVisibilityManager().getVisiblePlayerCount(player)))));
            player.sendMessage(lang.getMessage("stats.hidden_players", Map.of("count", String.valueOf(this.plugin.getVisibilityManager().getHiddenPlayerCount(player)))));
            player.sendMessage(lang.getMessage("stats.last_update", Map.of("time", String.valueOf(this.plugin.getPlayerManager().getLastUpdateTime(player)))));
        }
    }

    private void handleStrict(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String var7 = String.valueOf(ChatColor.GOLD);
            sender.sendMessage(var7 + "=== 엄격 모드 설정 ===");
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "현재 상태: " + String.valueOf(ChatColor.WHITE) + (this.plugin.isStrictModeEnabled() ? "활성화" : "비활성화"));
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "차단 임계값: " + String.valueOf(ChatColor.WHITE) + this.plugin.getBlockingThreshold() * (double)100.0F + "%");
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "다각도 감지: " + String.valueOf(ChatColor.WHITE) + (this.plugin.isMultiAngleCheckEnabled() ? "활성화" : "비활성화"));
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "다각도 반경: " + String.valueOf(ChatColor.WHITE) + this.plugin.getMultiAngleRadius());
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "사용법:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp strict enable/disable - 엄격 모드 활성화/비활성화");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp strict threshold <0.0-1.0> - 차단 임계값 설정");
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "임계값 설명:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.5 = 50%% 차단 시 숨김 (더 민감)");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.7 = 70%% 차단 시 숨김 (권장)");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.8 = 80%% 차단 시 숨김 (더 엄격)");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.9 = 90%% 차단 시 숨김 (가장 엄격)");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "1.0 = 연속 4블록 차단 시 숨김 (거의 완전 차단)");
        } else {
            String action = args[1].toLowerCase();
            if (!action.equals("enable") && !action.equals("on")) {
                if (!action.equals("disable") && !action.equals("off")) {
                    if (action.equals("threshold")) {
                        if (args.length < 3) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "사용법: /antiesp strict threshold <0.0-1.0>");
                            return;
                        }

                        try {
                            double threshold = Double.parseDouble(args[2]);
                            if (threshold < (double)0.0F || threshold > (double)1.0F) {
                                sender.sendMessage(String.valueOf(ChatColor.RED) + "임계값은 0.0에서 1.0 사이여야 합니다");
                                return;
                            }

                            this.plugin.getConfig().set("advanced.strict-mode.blocking-threshold", threshold);
                            this.plugin.saveConfig();
                            this.plugin.reloadConfiguration();
                            String var10001 = String.valueOf(ChatColor.GREEN);
                            sender.sendMessage(var10001 + "차단 임계값이 설정되었습니다: " + threshold * (double)100.0F + "%");
                        } catch (NumberFormatException var6) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "잘못된 숫자 형식입니다");
                        }
                    } else {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "잘못된 인수입니다! enable/disable/threshold를 사용하세요");
                    }
                } else {
                    this.plugin.getConfig().set("advanced.strict-mode.enabled", false);
                    this.plugin.saveConfig();
                    this.plugin.reloadConfiguration();
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "엄격 모드가 비활성화되었습니다");
                }
            } else {
                this.plugin.getConfig().set("advanced.strict-mode.enabled", true);
                this.plugin.saveConfig();
                this.plugin.reloadConfiguration();
                sender.sendMessage(String.valueOf(ChatColor.GREEN) + "엄격 모드가 활성화되었습니다");
            }

        }
    }

    private void handleTeam(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String var19 = String.valueOf(ChatColor.GOLD);
            sender.sendMessage(var19 + "=== 팀 면제 설정 ===");
            var19 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var19 + "현재 상태: " + String.valueOf(ChatColor.WHITE) + (this.plugin.isTeamExemptionEnabled() ? "활성화" : "비활성화"));
            var19 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var19 + "감지 방법: " + String.valueOf(ChatColor.WHITE) + this.plugin.getTeamDetectionMethod());
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "사용법:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp team enable/disable - 팀 면제 활성화/비활성화");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp team method <scoreboard|permission|both> - 감지 방법 설정");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp team info <플레이어명> - 플레이어 팀 정보 확인");
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "감지 방법 설명:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "scoreboard - 점수판 팀 시스템 사용");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "permission - 권한 시스템 사용 (antiesp.team.<teamname>)");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "both - 점수판과 권한 모두 확인");
        } else {
            String action = args[1].toLowerCase();
            if (!action.equals("enable") && !action.equals("on")) {
                if (!action.equals("disable") && !action.equals("off")) {
                    if (action.equals("method")) {
                        if (args.length < 3) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "사용법: /antiesp team method <scoreboard|permission|both>");
                            return;
                        }

                        String method = args[2].toLowerCase();
                        if (!method.equals("scoreboard") && !method.equals("permission") && !method.equals("both")) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "잘못된 감지 방법입니다! scoreboard, permission 또는 both를 사용하세요");
                        } else {
                            this.plugin.getConfig().set("team-exemption.detection-method", method);
                            this.plugin.saveConfig();
                            this.plugin.reloadConfiguration();
                            String var10001 = String.valueOf(ChatColor.GREEN);
                            sender.sendMessage(var10001 + "팀 감지 방법이 설정되었습니다: " + method);
                        }
                    } else if (action.equals("info")) {
                        if (args.length < 3) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "사용법: /antiesp team info <플레이어명>");
                            return;
                        }

                        String playerName = args[2];
                        Player targetPlayer = this.plugin.getServer().getPlayer(playerName);
                        if (targetPlayer == null) {
                            String var18 = String.valueOf(ChatColor.RED);
                            sender.sendMessage(var18 + "플레이어 " + playerName + " 은(는) 온라인 상태가 아니거나 존재하지 않습니다");
                            return;
                        }

                        sender.sendMessage(String.valueOf(ChatColor.GOLD) + "=== 플레이어 팀 정보 ===");
                        String var14 = String.valueOf(ChatColor.YELLOW);
                        sender.sendMessage(var14 + "플레이어: " + String.valueOf(ChatColor.WHITE) + targetPlayer.getName());

                        try {
                            Scoreboard scoreboard = targetPlayer.getScoreboard();
                            if (scoreboard != null) {
                                Team team = scoreboard.getPlayerTeam(targetPlayer);
                                var14 = String.valueOf(ChatColor.YELLOW);
                                sender.sendMessage(var14 + "점수판 팀: " + String.valueOf(ChatColor.WHITE) + (team != null ? team.getName() : "없음"));
                            }
                        } catch (Exception var10) {
                            var14 = String.valueOf(ChatColor.YELLOW);
                            sender.sendMessage(var14 + "점수판 팀: " + String.valueOf(ChatColor.RED) + "오류");
                        }

                        String permTeam = null;

                        for(PermissionAttachmentInfo perm : targetPlayer.getEffectivePermissions()) {
                            String permission = perm.getPermission();
                            if (permission.startsWith("antiesp.team.")) {
                                permTeam = permission.substring("antiesp.team.".length());
                                break;
                            }
                        }

                        var14 = String.valueOf(ChatColor.YELLOW);
                        sender.sendMessage(var14 + "권한 팀: " + String.valueOf(ChatColor.WHITE) + (permTeam != null ? permTeam : "없음"));
                    } else {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "잘못된 인수입니다! enable/disable/method/info를 사용하세요");
                    }
                } else {
                    this.plugin.getConfig().set("team-exemption.enabled", false);
                    this.plugin.saveConfig();
                    this.plugin.reloadConfiguration();
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "팀 면제가 비활성화되었습니다");
                }
            } else {
                this.plugin.getConfig().set("team-exemption.enabled", true);
                this.plugin.saveConfig();
                this.plugin.reloadConfiguration();
                sender.sendMessage(String.valueOf(ChatColor.GREEN) + "팀 면제가 활성화되었습니다");
            }

        }
    }

    private void sendHelpMessage(CommandSender sender) {
        if (sender instanceof Player player) {
            LanguageManager lang = this.plugin.getLanguageManager();
            player.sendMessage(lang.getMessage("commands.help.header"));
            String var10001 = lang.getMessage("commands.help.reload.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.reload.description"));
            var10001 = lang.getMessage("commands.help.status.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.status.description"));
            var10001 = lang.getMessage("commands.help.debug.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.debug.description"));
            var10001 = lang.getMessage("commands.help.info.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.info.description"));
            var10001 = lang.getMessage("commands.help.stats.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.stats.description"));
            var10001 = lang.getMessage("commands.help.strict.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.strict.description"));
            var10001 = lang.getMessage("commands.help.language.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.language.description"));
            var10001 = lang.getMessage("commands.help.team.usage");
            player.sendMessage(var10001 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.team.description"));
        } else {
            LanguageManager lang = this.plugin.getLanguageManager();
            sender.sendMessage(lang.getMessage("commands.help.header", "ko"));
            String var12 = lang.getMessage("commands.help.reload.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.reload.description", "ko"));
            var12 = lang.getMessage("commands.help.status.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.status.description", "ko"));
            var12 = lang.getMessage("commands.help.debug.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.debug.description", "ko"));
            var12 = lang.getMessage("commands.help.info.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.info.description", "ko"));
            var12 = lang.getMessage("commands.help.stats.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.stats.description", "ko"));
            var12 = lang.getMessage("commands.help.strict.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.strict.description", "ko"));
            var12 = lang.getMessage("commands.help.language.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.language.description", "ko"));
            var12 = lang.getMessage("commands.help.team.usage", "ko");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.team.description", "ko"));
        }

    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("antiesp.admin")) {
            return new ArrayList();
        } else if (args.length == 1) {
            List<String> subCommands = Arrays.asList("reload", "status", "debug", "info", "stats", "strict", "lang", "language", "team");
            List<String> completions = new ArrayList();

            for(String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }

            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            List<String> debugOptions = Arrays.asList("on", "off", "true", "false", "enable", "disable");
            List<String> completions = new ArrayList();

            for(String option : debugOptions) {
                if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(option);
                }
            }

            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("strict")) {
            List<String> strictOptions = Arrays.asList("enable", "disable", "on", "off", "threshold");
            List<String> completions = new ArrayList();

            for(String option : strictOptions) {
                if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(option);
                }
            }

            return completions;
        } else if (args.length != 2 || !args[0].equalsIgnoreCase("lang") && !args[0].equalsIgnoreCase("language")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("team")) {
                List<String> teamOptions = Arrays.asList("enable", "disable", "on", "off", "method", "info");
                List<String> completions = new ArrayList();

                for(String option : teamOptions) {
                    if (option.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(option);
                    }
                }

                return completions;
            } else if (args.length == 3 && args[0].equalsIgnoreCase("team") && args[1].equalsIgnoreCase("method")) {
                List<String> methodOptions = Arrays.asList("scoreboard", "permission", "both");
                List<String> completions = new ArrayList();

                for(String option : methodOptions) {
                    if (option.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(option);
                    }
                }

                return completions;
            } else if (args.length == 3 && args[0].equalsIgnoreCase("team") && args[1].equalsIgnoreCase("info")) {
                List<String> completions = new ArrayList();

                for(Player player : this.plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }

                return completions;
            } else {
                return new ArrayList();
            }
        } else {
            String[] supportedLangs = this.plugin.getLanguageManager().getSupportedLanguages();
            List<String> completions = new ArrayList();

            for(String lang : supportedLangs) {
                if (lang.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(lang);
                }
            }

            return completions;
        }
    }

    private void handleLanguage(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "이 명령어는 플레이어만 사용할 수 있습니다!");
        } else {
            LanguageManager lang = this.plugin.getLanguageManager();
            if (args.length < 2) {
                String currentLang = lang.getPlayerLanguage(player);
                String[] supportedLangs = lang.getSupportedLanguages();
                player.sendMessage(lang.getMessage("commands.language.current", Map.of("language", currentLang)));
                player.sendMessage(lang.getMessage("commands.language.supported", Map.of("languages", String.join(", ", supportedLangs))));
                player.sendMessage(lang.getMessage("commands.language.usage"));
            } else {
                String language = args[1].toLowerCase();
                if (lang.isLanguageSupported(language)) {
                    lang.setPlayerLanguage(player, language);
                    player.sendMessage(lang.getMessage("commands.language.changed", Map.of("language", language)));
                } else {
                    player.sendMessage(lang.getMessage("commands.language.not_supported", Map.of("language", language)));
                }

            }
        }
    }
}
