//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp;

import com.Antiesp.AntiEsp.commands.AntiEspCommand;
import com.Antiesp.AntiEsp.listeners.PlayerListener;
import com.Antiesp.AntiEsp.managers.LanguageManager;
import com.Antiesp.AntiEsp.managers.PacketManager;
import com.Antiesp.AntiEsp.managers.PlayerManager;
import com.Antiesp.AntiEsp.managers.VisibilityManager;
import com.Antiesp.AntiEsp.utils.RaycastUtils;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEsp extends JavaPlugin {
    private static AntiEsp instance;
    private PacketManager packetManager;
    private PlayerManager playerManager;
    private VisibilityManager visibilityManager;
    private LanguageManager languageManager;
    private boolean debugMode = false;
    private int checkInterval = 2;
    private double maxDistance = (double)64.0F;
    private double minDistance = (double)5.0F;
    private boolean enablePacketInterception = true;
    private boolean strictModeEnabled = true;
    private double blockingThreshold = 0.8;
    private boolean multiAngleCheck = true;
    private double multiAngleRadius = 0.3;
    private boolean useEyeHeight = true;
    private boolean teamExemptionEnabled = true;
    private String teamDetectionMethod = "scoreboard";

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.loadConfig();
        RaycastUtils.setDebugMode(this.debugMode);
        this.initializeManagers();
        this.registerListeners();
        this.registerCommands();
        this.startScheduler();
        this.getLogger().info("AntiESP 플러그인이 활성화되었습니다! 버전: " + this.getDescription().getVersion());
        this.getLogger().info("제작자: " + String.valueOf(this.getDescription().getAuthors()));
        if (this.debugMode) {
            this.getLogger().info("디버그 모드가 활성화되었습니다");
        }

    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        if (this.visibilityManager != null) {
            this.visibilityManager.cleanup();
        }

        if (this.playerManager != null) {
            this.playerManager.cleanup();
        }

        if (this.packetManager != null) {
            this.packetManager.cleanup();
        }

        if (this.languageManager != null) {
            this.languageManager.cleanup();
        }

        this.getLogger().info("AntiESP 플러그인이 비활성화되었습니다!");
    }

    private void initializeManagers() {
        try {
            this.languageManager = new LanguageManager(this);
            this.playerManager = new PlayerManager(this);
            this.packetManager = new PacketManager(this);
            this.visibilityManager = new VisibilityManager(this);
            this.getLogger().info("모든 매니저 초기화 완료");
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "매니저 초기화 중 오류 발생", e);
            this.getServer().getPluginManager().disablePlugin(this);
        }

    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getLogger().info("이벤트 리스너 등록 완료");
    }

    private void registerCommands() {
        this.getCommand("antiesp").setExecutor(new AntiEspCommand(this));
        this.getLogger().info("명령어 등록 완료");
    }

    private void startScheduler() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (this.visibilityManager != null) {
                this.visibilityManager.updateVisibility();
            }

        }, 0L, (long)this.checkInterval);
        this.getLogger().info("스케줄러 시작됨, 검사 간격: " + this.checkInterval + " 틱");
    }

    private void loadConfig() {
        this.debugMode = this.getConfig().getBoolean("debug", false);
        this.checkInterval = this.getConfig().getInt("check-interval", 2);
        this.maxDistance = this.getConfig().getDouble("max-distance", (double)64.0F);
        this.minDistance = this.getConfig().getDouble("min-distance", (double)5.0F);
        this.enablePacketInterception = this.getConfig().getBoolean("enable-packet-interception", true);
        this.strictModeEnabled = this.getConfig().getBoolean("advanced.strict-mode.enabled", true);
        this.blockingThreshold = this.getConfig().getDouble("advanced.strict-mode.blocking-threshold", 0.8);
        this.multiAngleCheck = this.getConfig().getBoolean("advanced.strict-mode.multi-angle-check", true);
        this.multiAngleRadius = this.getConfig().getDouble("advanced.strict-mode.multi-angle-radius", 0.3);
        this.useEyeHeight = this.getConfig().getBoolean("advanced.strict-mode.use-eye-height", true);
        this.teamExemptionEnabled = this.getConfig().getBoolean("team-exemption.enabled", true);
        this.teamDetectionMethod = this.getConfig().getString("team-exemption.detection-method", "scoreboard");
        RaycastUtils.setDebugMode(this.debugMode);
        this.getLogger().info("설정 파일 로드 완료");
        if (this.strictModeEnabled) {
            this.getLogger().info("엄격 모드 활성화됨 - 차단 임계값: " + this.blockingThreshold * (double)100.0F + "%");
        }

        if (this.debugMode) {
            this.getLogger().info("디버그 모드 활성화됨 - 상세 출력 켜짐");
        }

        if (this.teamExemptionEnabled) {
            this.getLogger().info("팀 면제 활성화됨 - 감지 방법: " + this.teamDetectionMethod);
        }

    }

    public void reloadConfiguration() {
        this.reloadConfig();
        this.loadConfig();
        if (this.visibilityManager != null) {
            this.visibilityManager.reloadConfig();
        }

        this.getLogger().info("설정이 다시 로드되었습니다");
    }

    public static AntiEsp getInstance() {
        return instance;
    }

    public PacketManager getPacketManager() {
        return this.packetManager;
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public VisibilityManager getVisibilityManager() {
        return this.visibilityManager;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public int getCheckInterval() {
        return this.checkInterval;
    }

    public double getMaxDistance() {
        return this.maxDistance;
    }

    public double getMinDistance() {
        return this.minDistance;
    }

    public boolean isPacketInterceptionEnabled() {
        return this.enablePacketInterception;
    }

    public boolean isStrictModeEnabled() {
        return this.strictModeEnabled;
    }

    public double getBlockingThreshold() {
        return this.blockingThreshold;
    }

    public boolean isMultiAngleCheckEnabled() {
        return this.multiAngleCheck;
    }

    public double getMultiAngleRadius() {
        return this.multiAngleRadius;
    }

    public boolean isUseEyeHeight() {
        return this.useEyeHeight;
    }

    public boolean isTeamExemptionEnabled() {
        return this.teamExemptionEnabled;
    }

    public String getTeamDetectionMethod() {
        return this.teamDetectionMethod;
    }
}
