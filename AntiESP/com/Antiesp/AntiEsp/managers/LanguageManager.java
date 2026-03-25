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
        this.defaultLanguage = "zh";
        this.initializeLanguages();
    }

    private void initializeLanguages() {
        File languageDir = new File(this.plugin.getDataFolder(), "languages");
        if (!languageDir.exists()) {
            languageDir.mkdirs();
        }

        this.loadLanguageFile("zh", "messages_zh.yml");
        this.loadLanguageFile("en", "messages_en.yml");
        this.plugin.getLogger().info("语言管理器初始化完成，支持语言: " + String.valueOf(this.languages.keySet()));
    }

    private void loadLanguageFile(String languageCode, String fileName) {
        try {
            File languageFile = new File(this.plugin.getDataFolder(), "languages/" + fileName);
            if (!languageFile.exists()) {
                InputStream resourceStream = this.plugin.getResource(fileName);
                if (resourceStream == null) {
                    this.plugin.getLogger().warning("找不到语言文件资源: " + fileName);
                    return;
                }

                Files.copy(resourceStream, languageFile.toPath(), new CopyOption[0]);
                this.plugin.getLogger().info("已创建语言文件: " + fileName);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
            this.languages.put(languageCode, config);
            this.plugin.getLogger().info("已加载语言文件: " + languageCode);
        } catch (IOException e) {
            this.plugin.getLogger().warning("加载语言文件失败: " + fileName + " - " + e.getMessage());
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
            this.plugin.getLogger().warning("不支持的语言: " + language);
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
            this.plugin.getLogger().warning("不支持的语言: " + language);
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
        this.plugin.getLogger().info("语言文件已重新加载");
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
        this.plugin.getLogger().info("语言管理器已清理");
    }
}
