package io.github.unknownredgreen;

import io.github.unknownredgreen.files.ConfigFileManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
final class Bot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private long botStartTimeInSeconds;
    private final List<String> data;
    private final ConfigFileManager configFileManager;
    private final Random random;
    @Getter
    private int maxDataLength;
    private String[] stickerIds;
    private boolean canSendStickers = true;
    private final Map<Long, Integer> chatLimits = new HashMap<>();
    private BotActionsWrapper actions;

    @Override
    public void onRegister() {
        botStartTimeInSeconds = System.currentTimeMillis()/1000;
        try {
            maxDataLength = configFileManager.parseInt("maxDataLength");
            //i don`t think you even need < 5 data saved
            if (maxDataLength < 5) {
                throw new RuntimeException("Max data length can`t be < 5. Change maxDataLength in %s".formatted(configFileManager.getConfigFilePath()));
            }
        } catch (NumberFormatException | NullPointerException e) {
            log.debug("No maxDataLength found in config. Using default: 1000");
            maxDataLength = 1000;
        }
        try {
            stickerIds = configFileManager.parseStringArray("stickerIds");
        } catch (NullPointerException e) {
            log.debug("No stickerIds found in config. Using default: no sticker sending");
            canSendStickers = false;
        }
        // Initialize after got stickerIds
        actions = new BotActionsWrapper(this, random, getData(), getStickerIds());
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public String[] getStickerIds() {
        return stickerIds.clone();
    }

    public List<String> getData() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        Message msg = update.getMessage();
        if ((long) msg.getDate() < botStartTimeInSeconds) return;
        if (!msg.hasText()) return;
        long chatId = msg.getChatId();
        String text = msg.getText().replaceAll("\\R", " ");

        chatLimits.put(chatId, chatLimits.getOrDefault(chatId, 0)+1);

        updateData(text);

        if (data.size() < 5) return;

        try {
            if (chatId == msg.getFrom().getId()) {
                makeRandomAction(msg, false);
                return;
            }

            if (msg.getReplyToMessage() != null
                    &&
                msg.getReplyToMessage().getFrom().getId().equals(getMe().getId())
            ) {
                makeRandomAction(msg, true);
                return;
            }

            if (text.contains("@" + getMe().getUserName())) {
                makeRandomAction(msg, true);
                return;
            }

            if (chatLimits.get(chatId) > 20 && random.nextInt(0, 5) == 0) {
                makeRandomAction(msg, false);
                chatLimits.put(chatId, 0);
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void makeRandomAction(Message msg, boolean isReplyGuaranteed) throws TelegramApiException {
        int randomNum = random.nextInt(0, 20);
        if (randomNum == 0) {
            if (canSendStickers) actions.sendRandomSticker(msg);
            else actions.sendRandomMessage(msg, isReplyGuaranteed);
        } else {
            actions.sendRandomMessage(msg, isReplyGuaranteed);
        }
    }

    private void updateData(String str) {
        if (str.length() > 100) return;
        if (data.contains(str)) return;

        if (random.nextInt(0, 10) == 9) {
            str = str.replace(" ", "");
        }

        int size = data.size();

        if (size < maxDataLength) {
            data.add(str);
        } else if (size == maxDataLength) {
            data.set(random.nextInt(0, maxDataLength), str);
        } else {
            while (data.size() > maxDataLength) {
                data.removeLast();
            }
        }
    }
}