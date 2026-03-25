//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.managers;

import com.Antiesp.AntiEsp.AntiEsp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LanguageManager {
    private final AntiEsp plugin;
    private final Map<String, FileConfiguration> languages;
    private final Map<UUID, String> playerLanguages;
    private String defaultLanguage;

    public LanguageManager(AntiEsp plugin) {
        this.plugin = plugin;
        this.languages = new HashMap();
        this.playerLanguages = new HashMap();
        this.defaultLanguage = "ko";
        this.initializeLanguages();
    }

    private void initializeLanguages() {
        File languageDir = new File(this.plugin.getDataFolder(), "languages");
        if (!languageDir.exists()) {
            languageDir.mkdirs();
        }

        this.loadLanguageFile("ko", "messages_ko.yml");
        this.loadLanguageFile("en", "messages_en.yml");
        this.plugin.getLogger().info("언어 매니저 초기화 완료, 지원 언어: " + String.valueOf(this.languages.keySet()));
    }

    private void loadLanguageFile(String languageCode, String fileName) {
        try {
            File languageFile = new File(this.plugin.getDataFolder(), "languages/" + fileName);
            if (!languageFile.exists()) {
                InputStream resourceStream = this.plugin.getResource(fileName);
                if (resourceStream == null) {
                    this.plugin.getLogger().warning("언어 파일 리소스를 찾을 수 없습니다: " + fileName);
                    return;
                }

                Files.copy(resourceStream, languageFile.toPath(), new CopyOption[0]);
                this.plugin.getLogger().info("언어 파일 생성됨: " + fileName);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
            this.languages.put(languageCode, config);
            this.plugin.getLogger().info("언어 파일 로드됨: " + languageCode);
        } catch (IOException e) {
            this.plugin.getLogger().warning("언어 파일 로드 실패: " + fileName + " - " + e.getMessage());
        }

    }

    public String getMessage(String key, String language) {
        FileConfiguration config = (FileConfiguration)this.languages.get(language);
        if (config == null) {
            config = (FileConfiguration)this.languages.get(this.defaultLanguage);
        }

        if (config == null) {
            return "&cMessage not found: " + key;
        } else {
            String message = config.getString(key);
            return message == null ? "&cMessage not found: " + key : ChatColor.translateAlternateColorCodes('&', message);
        }
    }

    public String getMessage(String key) {
        return this.getMessage(key, this.defaultLanguage);
    }

    public String getMessage(String key, Player player) {
        String language = this.getPlayerLanguage(player);
        return this.getMessage(key, language);
    }

    public String getMessage(String key, String language, Map<String, String> placeholders) {
        String message = this.getMessage(key, language);
        if (placeholders != null) {
            for(Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + (String)entry.getKey() + "}", (CharSequence)entry.getValue());
            }
        }

        return message;
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        return this.getMessage(key, this.defaultLanguage, placeholders);
    }

    public String getMessage(String key, Player player, Map<String, String> placeholders) {
        String language = this.getPlayerLanguage(player);
        return this.getMessage(key, language, placeholders);
    }

    public void setPlayerLanguage(Player player, String language) {
        if (this.languages.containsKey(language)) {
            this.playerLanguages.put(player.getUniqueId(), language);
        } else {
            this.plugin.getLogger().warning("지원하지 않는 언어입니다: " + language);
        }

    }

    public String getPlayerLanguage(Player player) {
        return (String)this.playerLanguages.getOrDefault(player.getUniqueId(), this.defaultLanguage);
    }

    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }

    public void setDefaultLanguage(String language) {
        if (this.languages.containsKey(language)) {
            this.defaultLanguage = language;
        } else {
            this.plugin.getLogger().warning("지원하지 않는 언어입니다: " + language);
        }

    }

    public String[] getSupportedLanguages() {
        return (String[])this.languages.keySet().toArray(new String[0]);
    }

    public boolean isLanguageSupported(String language) {
        return this.languages.containsKey(language);
    }

    public void reloadLanguages() {
        this.languages.clear();
        this.initializeLanguages();
        this.plugin.getLogger().info("언어 파일이 다시 로드되었습니다");
    }

    public void sendMessage(Player player, String key) {
        String message = this.getMessage(key, player);
        player.sendMessage(message);
    }

    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        String message = this.getMessage(key, player, placeholders);
        player.sendMessage(message);
    }

    public void sendMessage(Player player, String key, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap();
        placeholders.put(placeholder, value);
        this.sendMessage(player, key, placeholders);
    }

    public void sendMessage(Player player, String key, String placeholder1, String value1, String placeholder2, String value2) {
        Map<String, String> placeholders = new HashMap();
        placeholders.put(placeholder1, value1);
        placeholders.put(placeholder2, value2);
        this.sendMessage(player, key, placeholders);
    }

    public void clearPlayerLanguage(UUID playerId) {
        this.playerLanguages.remove(playerId);
    }

    public String getPrefix() {
        return this.getMessage("plugin.prefix");
    }

    public String getPrefix(Player player) {
        return this.getMessage("plugin.prefix", player);
    }

    public String formatMessage(String message) {
        String var10000 = this.getPrefix();
        return var10000 + message;
    }

    public String formatMessage(String message, Player player) {
        String var10000 = this.getPrefix(player);
        return var10000 + message;
    }

    public void cleanup() {
        this.languages.clear();
        this.playerLanguages.clear();
        this.plugin.getLogger().info("언어 매니저가 정리되었습니다");
    }
}
