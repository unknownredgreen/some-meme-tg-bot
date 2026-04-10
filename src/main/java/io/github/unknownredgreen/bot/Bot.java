package io.github.unknownredgreen.bot;

import io.github.unknownredgreen.ConfigStorage;
import io.github.unknownredgreen.EveryHourStatsLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public final class Bot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private final List<String> data;
    private final ConfigStorage configStorage;
    private final Random random;
    private final EveryHourStatsLog everyHourStatsLog;
    private final Map<Long, Integer> chatLimits = new HashMap<>();

    private Map<String, String> reactionEmojisByEqualsICAndEmoji;
    private BotActions actions;
    private User me;
    private boolean sendingStickers;
    private boolean reactingToMessages;
    private boolean reactingToMessagesByEqualsIC;
    private long botStartTimeInSeconds;
    private int maxDataLength;

    @Override
    public void onRegister() {
        botStartTimeInSeconds = System.currentTimeMillis()/1000;
        try {
            me = getMe();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        sendingStickers = configStorage.isSendingStickers();
        reactingToMessages = configStorage.isReactingToMessages();
        reactingToMessagesByEqualsIC = configStorage.isReactingToMessagesByEqualsIC();
        maxDataLength = configStorage.getMaxDataLength();
        reactionEmojisByEqualsICAndEmoji = configStorage.getReactionEmojisByEqualsICAndEmoji();
        actions = new BotActions(this, random, getData(), configStorage, everyHourStatsLog);
    }

    @Override
    public void onClosing() {
        everyHourStatsLog.stopLogging();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public List<String> getData() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        Message msg = update.getMessage();
        if ((long) msg.getDate() < botStartTimeInSeconds) return;
        if (reactingToMessages && random.nextInt(10) == 0) actions.setRandomReaction(msg);
        if (!msg.hasText()) return;

        handleMessage(msg);
    }

    private void handleMessage(Message msg) {
        everyHourStatsLog.incrementIntValue("Messages handled");
        long chatId = msg.getChatId();
        String text = msg.getText().replaceAll("\\R", " ");

        if (reactingToMessagesByEqualsIC && reactionEmojisByEqualsICAndEmoji.containsKey(text.toLowerCase())) {
            actions.setReaction(
                    msg,
                    reactionEmojisByEqualsICAndEmoji.get(text.toLowerCase())
            );
        }

        chatLimits.put(chatId, chatLimits.getOrDefault(chatId, 0)+1); //message counter increment

        updateData(text);

        if (data.isEmpty()) return;

        if (chatId == msg.getFrom().getId()) {
            makeRandomAction(msg, false, text);
            return;
        }

        if (msg.getReplyToMessage() != null
                &&
            msg.getReplyToMessage().getFrom().getId().equals(me.getId())
        ) {
            makeRandomAction(msg, true, text);
            return;
        }

        if (text.contains("@" + getBotUsername())) {
            makeRandomAction(msg, true, text);
            switch (random.nextInt(2)) {
                case 0: actions.setReaction(msg, "\uD83D\uDC4D"); break; //like
                case 1: actions.setReaction(msg, "\uD83D\uDC4E"); break; //dislike
            }
            return;
        }

        if (chatLimits.get(chatId) > 20 && (random.nextInt(5) == 0)) {
            //if more than 20 messages passed after the last bot random actions
            //with 20% chance make a random action
            makeRandomAction(msg, false, text);
            chatLimits.put(chatId, 0);
        }
    }

    private void makeRandomAction(Message msg, boolean isReplyGuaranteed, String filteredText) {
        int randomNum = random.nextInt(20);
        if (randomNum == 0) {
            if (sendingStickers) actions.sendRandomSticker(msg);
            else actions.sendRandomMessage(msg, isReplyGuaranteed, filteredText);
        } else {
            actions.sendRandomMessage(msg, isReplyGuaranteed, filteredText);
        }
    }

    private void updateData(String str) {
        if (str.length() > 100) return;
        if (data.contains(str)) return;

        int size = data.size();

        if (size < maxDataLength) {
            data.add(str);
        } else if (size == maxDataLength) {
            data.set(random.nextInt(maxDataLength), str);
        } else {
            while (data.size() > maxDataLength) {
                data.removeLast();
            }
        }
        everyHourStatsLog.put("Current in-ram data size", data.size());
    }
}