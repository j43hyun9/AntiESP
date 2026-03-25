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
            sender.sendMessage(String.valueOf(ChatColor.RED) + "你没有权限使用此命令！");
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
                String successMessage = this.plugin.getLanguageManager().getMessage("commands.reload.success", "zh");
                sender.sendMessage(successMessage);
            }
        } catch (Exception var4) {
            if (sender instanceof Player) {
                String errorMessage = this.plugin.getLanguageManager().getMessage("commands.reload.failed", Map.of("error", var4.getMessage()));
                sender.sendMessage(errorMessage);
            } else {
                String errorMessage = this.plugin.getLanguageManager().getMessage("commands.reload.failed", "zh", Map.of("error", var4.getMessage()));
                sender.sendMessage(errorMessage);
            }

            this.plugin.getLogger().warning("重载配置时发生错误: " + var4.getMessage());
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
            sender.sendMessage(lang.getMessage("status.header", "zh"));
            sender.sendMessage(lang.getMessage("status.version", "zh", Map.of("version", this.plugin.getDescription().getVersion())));
            sender.sendMessage(lang.getMessage("status.author", "zh", Map.of("author", String.join(", ", this.plugin.getDescription().getAuthors()))));
            sender.sendMessage(lang.getMessage("status.debug_mode", "zh", Map.of("status", this.plugin.isDebugMode() ? lang.getMessage("common.enabled", "zh") : lang.getMessage("common.disabled", "zh"))));
            sender.sendMessage(lang.getMessage("status.check_interval", "zh", Map.of("interval", String.valueOf(this.plugin.getCheckInterval()))));
            sender.sendMessage(lang.getMessage("status.max_distance", "zh", Map.of("distance", String.valueOf(this.plugin.getMaxDistance()))));
            sender.sendMessage(lang.getMessage("status.min_distance", "zh", Map.of("distance", String.valueOf(this.plugin.getMinDistance()))));
            sender.sendMessage(lang.getMessage("status.packet_interception", "zh", Map.of("status", this.plugin.isPacketInterceptionEnabled() ? lang.getMessage("common.enabled", "zh") : lang.getMessage("common.disabled", "zh"))));
            sender.sendMessage(lang.getMessage("status.online_players", "zh", Map.of("count", String.valueOf(this.plugin.getServer().getOnlinePlayers().size()))));
        }

    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player) {
                this.plugin.getLanguageManager().sendMessage((Player)sender, "commands.debug.usage");
            } else {
                String usageMessage = this.plugin.getLanguageManager().getMessage("commands.debug.usage", "zh");
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
                        String usageMessage = this.plugin.getLanguageManager().getMessage("commands.debug.usage", "zh");
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
                    String enabledMessage = lang.getMessage("commands.debug.enabled", "zh");
                    sender.sendMessage(enabledMessage);
                } else {
                    String disabledMessage = lang.getMessage("commands.debug.disabled", "zh");
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
            sender.sendMessage(lang.getMessage("info.header", "zh"));
            sender.sendMessage(lang.getMessage("info.description", "zh"));
            sender.sendMessage(lang.getMessage("info.description2", "zh"));
            sender.sendMessage(lang.getMessage("info.description3", "zh"));
            sender.sendMessage("");
            sender.sendMessage(lang.getMessage("info.features.header", "zh"));
            sender.sendMessage(lang.getMessage("info.features.real_time", "zh"));
            sender.sendMessage(lang.getMessage("info.features.packet_interception", "zh"));
            sender.sendMessage(lang.getMessage("info.features.player_hiding", "zh"));
            sender.sendMessage(lang.getMessage("info.features.performance_optimization", "zh"));
            sender.sendMessage(lang.getMessage("info.features.close_distance_exemption", "zh"));
            sender.sendMessage(lang.getMessage("info.features.multi_angle_detection", "zh"));
            sender.sendMessage(lang.getMessage("info.features.eye_height_detection", "zh"));
        }

    }

    private void handleStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            String playerOnlyMessage = this.plugin.getLanguageManager().getMessage("commands.player_only", "zh");
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
            sender.sendMessage(var7 + "=== 严格模式设置 ===");
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "当前状态: " + String.valueOf(ChatColor.WHITE) + (this.plugin.isStrictModeEnabled() ? "启用" : "禁用"));
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "阻挡阈值: " + String.valueOf(ChatColor.WHITE) + this.plugin.getBlockingThreshold() * (double)100.0F + "%");
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "多角度检测: " + String.valueOf(ChatColor.WHITE) + (this.plugin.isMultiAngleCheckEnabled() ? "启用" : "禁用"));
            var7 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var7 + "多角度半径: " + String.valueOf(ChatColor.WHITE) + this.plugin.getMultiAngleRadius());
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "用法:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp strict enable/disable - 启用/禁用严格模式");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp strict threshold <0.0-1.0> - 设置阻挡阈值");
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "阈值说明:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.5 = 50%%阻挡就隐藏（更敏感）");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.7 = 70%%阻挡才隐藏（推荐）");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.8 = 80%%阻挡才隐藏（更严格）");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "0.9 = 90%%阻挡才隐藏（最严格）");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "1.0 = 连续4格阻挡才隐藏（几乎完全阻挡）");
        } else {
            String action = args[1].toLowerCase();
            if (!action.equals("enable") && !action.equals("on")) {
                if (!action.equals("disable") && !action.equals("off")) {
                    if (action.equals("threshold")) {
                        if (args.length < 3) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "用法: /antiesp strict threshold <0.0-1.0>");
                            return;
                        }

                        try {
                            double threshold = Double.parseDouble(args[2]);
                            if (threshold < (double)0.0F || threshold > (double)1.0F) {
                                sender.sendMessage(String.valueOf(ChatColor.RED) + "阈值必须在0.0到1.0之间");
                                return;
                            }

                            this.plugin.getConfig().set("advanced.strict-mode.blocking-threshold", threshold);
                            this.plugin.saveConfig();
                            this.plugin.reloadConfiguration();
                            String var10001 = String.valueOf(ChatColor.GREEN);
                            sender.sendMessage(var10001 + "阻挡阈值已设置为: " + threshold * (double)100.0F + "%");
                        } catch (NumberFormatException var6) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "无效的数字格式");
                        }
                    } else {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "无效的参数！使用 enable/disable/threshold");
                    }
                } else {
                    this.plugin.getConfig().set("advanced.strict-mode.enabled", false);
                    this.plugin.saveConfig();
                    this.plugin.reloadConfiguration();
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "严格模式已禁用");
                }
            } else {
                this.plugin.getConfig().set("advanced.strict-mode.enabled", true);
                this.plugin.saveConfig();
                this.plugin.reloadConfiguration();
                sender.sendMessage(String.valueOf(ChatColor.GREEN) + "严格模式已启用");
            }

        }
    }

    private void handleTeam(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String var19 = String.valueOf(ChatColor.GOLD);
            sender.sendMessage(var19 + "=== 队伍豁免设置 ===");
            var19 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var19 + "当前状态: " + String.valueOf(ChatColor.WHITE) + (this.plugin.isTeamExemptionEnabled() ? "启用" : "禁用"));
            var19 = String.valueOf(ChatColor.YELLOW);
            sender.sendMessage(var19 + "检测方法: " + String.valueOf(ChatColor.WHITE) + this.plugin.getTeamDetectionMethod());
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "用法:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp team enable/disable - 启用/禁用队伍豁免");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp team method <scoreboard|permission|both> - 设置检测方法");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "/antiesp team info <玩家名> - 查看玩家队伍信息");
            sender.sendMessage("" + String.valueOf(ChatColor.YELLOW));
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "检测方法说明:");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "scoreboard - 使用计分板队伍系统");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "permission - 使用权限系统 (antiesp.team.<teamname>)");
            sender.sendMessage(String.valueOf(ChatColor.WHITE) + "both - 同时检查计分板和权限");
        } else {
            String action = args[1].toLowerCase();
            if (!action.equals("enable") && !action.equals("on")) {
                if (!action.equals("disable") && !action.equals("off")) {
                    if (action.equals("method")) {
                        if (args.length < 3) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "用法: /antiesp team method <scoreboard|permission|both>");
                            return;
                        }

                        String method = args[2].toLowerCase();
                        if (!method.equals("scoreboard") && !method.equals("permission") && !method.equals("both")) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "无效的检测方法！使用 scoreboard、permission 或 both");
                        } else {
                            this.plugin.getConfig().set("team-exemption.detection-method", method);
                            this.plugin.saveConfig();
                            this.plugin.reloadConfiguration();
                            String var10001 = String.valueOf(ChatColor.GREEN);
                            sender.sendMessage(var10001 + "队伍检测方法已设置为: " + method);
                        }
                    } else if (action.equals("info")) {
                        if (args.length < 3) {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "用法: /antiesp team info <玩家名>");
                            return;
                        }

                        String playerName = args[2];
                        Player targetPlayer = this.plugin.getServer().getPlayer(playerName);
                        if (targetPlayer == null) {
                            String var18 = String.valueOf(ChatColor.RED);
                            sender.sendMessage(var18 + "玩家 " + playerName + " 不在线或不存在");
                            return;
                        }

                        sender.sendMessage(String.valueOf(ChatColor.GOLD) + "=== 玩家队伍信息 ===");
                        String var14 = String.valueOf(ChatColor.YELLOW);
                        sender.sendMessage(var14 + "玩家: " + String.valueOf(ChatColor.WHITE) + targetPlayer.getName());

                        try {
                            Scoreboard scoreboard = targetPlayer.getScoreboard();
                            if (scoreboard != null) {
                                Team team = scoreboard.getPlayerTeam(targetPlayer);
                                var14 = String.valueOf(ChatColor.YELLOW);
                                sender.sendMessage(var14 + "计分板队伍: " + String.valueOf(ChatColor.WHITE) + (team != null ? team.getName() : "无"));
                            }
                        } catch (Exception var10) {
                            var14 = String.valueOf(ChatColor.YELLOW);
                            sender.sendMessage(var14 + "计分板队伍: " + String.valueOf(ChatColor.RED) + "错误");
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
                        sender.sendMessage(var14 + "权限队伍: " + String.valueOf(ChatColor.WHITE) + (permTeam != null ? permTeam : "无"));
                    } else {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "无效的参数！使用 enable/disable/method/info");
                    }
                } else {
                    this.plugin.getConfig().set("team-exemption.enabled", false);
                    this.plugin.saveConfig();
                    this.plugin.reloadConfiguration();
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "队伍豁免已禁用");
                }
            } else {
                this.plugin.getConfig().set("team-exemption.enabled", true);
                this.plugin.saveConfig();
                this.plugin.reloadConfiguration();
                sender.sendMessage(String.valueOf(ChatColor.GREEN) + "队伍豁免已启用");
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
            sender.sendMessage(lang.getMessage("commands.help.header", "zh"));
            String var12 = lang.getMessage("commands.help.reload.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.reload.description", "zh"));
            var12 = lang.getMessage("commands.help.status.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.status.description", "zh"));
            var12 = lang.getMessage("commands.help.debug.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.debug.description", "zh"));
            var12 = lang.getMessage("commands.help.info.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.info.description", "zh"));
            var12 = lang.getMessage("commands.help.stats.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.stats.description", "zh"));
            var12 = lang.getMessage("commands.help.strict.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.strict.description", "zh"));
            var12 = lang.getMessage("commands.help.language.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.language.description", "zh"));
            var12 = lang.getMessage("commands.help.team.usage", "zh");
            sender.sendMessage(var12 + String.valueOf(ChatColor.WHITE) + " - " + lang.getMessage("commands.help.team.description", "zh"));
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
            sender.sendMessage(String.valueOf(ChatColor.RED) + "此命令只能由玩家使用！");
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
