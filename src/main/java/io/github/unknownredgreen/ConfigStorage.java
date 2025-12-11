package io.github.unknownredgreen;

import io.github.unknownredgreen.files.ConfigFileManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class ConfigStorage {
    private final ConfigFileManager configFileManager;
    private String[] stickerIds;
    private String[] reactionEmojis;
    private int maxDataLength;
    private boolean sendingStickers = true;
    private boolean reactingToMessages = true;

    public ConfigStorage(ConfigFileManager configFileManager) {
        this.configFileManager = configFileManager;

        try {
            maxDataLength = configFileManager.parseInt("maxDataLength");
            //i don`t think you even need < 10 data saved
            if (maxDataLength < 10) {
                throw new RuntimeException("Max data length can`t be < 10. Change maxDataLength in %s".formatted(configFileManager.getConfigFilePath()));
            }
        } catch (NumberFormatException | NullPointerException e) {
            log.info("No maxDataLength found in config. Using default: 1000");
            maxDataLength = 1000;
        }

        try {
            stickerIds = configFileManager.parseStringArray("stickerIds");
        } catch (NullPointerException e) {
            log.info("No stickerIds found in config. Using default: no sticker sending");
            sendingStickers = false;
        }

        try {
            reactionEmojis = configFileManager.parseStringArray("reactionEmojis");
        } catch (NullPointerException e) {
            log.info("No reactionEmojis found in config. Using default: no reacting to messages");
            reactingToMessages = false;
        }


        try {
            sendingStickers = configFileManager.parseBoolean("sendingStickers");
        } catch (NullPointerException e) {}
        if (stickerIds == null || stickerIds.length == 0) {
            sendingStickers = false;
        }

        try {
            reactingToMessages = configFileManager.parseBoolean("reactingToMessages");
        } catch (NullPointerException e) {}
        if (reactionEmojis == null || reactionEmojis.length == 0) {
            reactingToMessages = false;
        }
    }
}