package io.github.unknownredgreen;

import io.github.unknownredgreen.files.ConfigFileManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public final class ConfigStorage {
    private final ConfigFileManager configFileManager;
    private String[] stickerIds;
    private String[] reactionEmojis;
    private int maxDataLength;
    private boolean sendingStickers = true;
    private boolean reactingToMessages = true;
    private boolean reactingToMessagesByEqualsIC = true;

    @Getter(AccessLevel.NONE)
    private Map<String, String> reactionEmojisByEqualsICAndEmoji; //where IC = Ignore case

    public Map<String, String> getReactionEmojisByEqualsICAndEmoji() {
        return Collections.unmodifiableMap(reactionEmojisByEqualsICAndEmoji);
    }

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
            sendingStickers = configFileManager.parseBoolean("sendingStickers");
        } catch (NullPointerException ignored) {}

        try {
            reactingToMessages = configFileManager.parseBoolean("reactingToMessages");
        } catch (NullPointerException ignored) {}

        try {
            reactingToMessagesByEqualsIC = configFileManager.parseBoolean("reactingEmojisByEqualsIC");
        } catch (NullPointerException ignored) {}

        try {
            stickerIds = configFileManager.parseStringArray("stickerIds");
        } catch (NullPointerException e) {
            if (sendingStickers) {
                log.info("No stickerIds found in config. Using default: no sticker sending");
                sendingStickers = false;
            }
        }

        try {
            reactionEmojis = configFileManager.parseStringArray("reactionEmojis");
        } catch (NullPointerException e) {
            if (reactingToMessages) {
                log.info("No reactionEmojis found in config. Using default: no reacting to messages");
                reactingToMessages = false;
            }
        }

        try {
            reactionEmojisByEqualsICAndEmoji = configFileManager.parseMapStringString("reactionEmojisByEqualsICAndEmoji");
        } catch (NullPointerException e) {
            if (reactingToMessagesByEqualsIC) {
                log.info("No reactionEmojisByEqualsICAndEmoji found in config. Using default: no reacting to messages by equals ignore case");
                reactingToMessagesByEqualsIC = false;
            }
        }

        if (stickerIds == null || stickerIds.length == 0) {
            sendingStickers = false;
        }
        if (reactionEmojis == null || reactionEmojis.length == 0) {
            reactingToMessages = false;
        }
        if (reactionEmojisByEqualsICAndEmoji == null || reactionEmojisByEqualsICAndEmoji.isEmpty()) {
            reactingToMessagesByEqualsIC = false;
        }

        Map<String, String> reactionEmojisByEqualsICAndEmojiTemp = new HashMap<>();

        for (var entry : reactionEmojisByEqualsICAndEmoji.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            reactionEmojisByEqualsICAndEmojiTemp.put(key.toLowerCase(), value);
        }
        reactionEmojisByEqualsICAndEmoji.clear();
        reactionEmojisByEqualsICAndEmoji.putAll(reactionEmojisByEqualsICAndEmojiTemp);
    }
}