package io.github.unknownredgreen;

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
final class Bot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private final List<String> data;
    private final ConfigStorage configStorage;
    private final Random random;
    private final Map<Long, Integer> chatLimits = new HashMap<>();

    private BotActionsWrapper actions;
    private User me;
    private boolean sendingStickers;
    private boolean reactingToMessages;
    private long botStartTimeInSeconds;
    private int maxDataLength;

    @Override
    public void onRegister() {
        botStartTimeInSeconds = System.currentTimeMillis()/1000;
        try {
            me = getMe();
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        sendingStickers = configStorage.isSendingStickers();
        reactingToMessages = configStorage.isReactingToMessages();
        maxDataLength = configStorage.getMaxDataLength();
        actions = new BotActionsWrapper(this, random, getData(), configStorage);
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

        if (reactingToMessages && random.nextInt(0, 10) == 0) actions.setRandomReaction(msg);

        if (!msg.hasText()) return;
        long chatId = msg.getChatId();
        String text = msg.getText().replaceAll("\\R", " ");
        chatLimits.put(chatId, chatLimits.getOrDefault(chatId, 0)+1);

        updateData(text);

        if (data.size() < 5) return;

        if (chatId == msg.getFrom().getId()) {
            makeRandomAction(msg, false, text);
            return;
        }

        if (
            me != null
                &&
            msg.getReplyToMessage() != null
                &&
            msg.getReplyToMessage().getFrom().getId().equals(me.getId())
        ) {
            makeRandomAction(msg, true, text);
            return;
        }

        if (me != null && text.contains("@" + getBotUsername())) {
            makeRandomAction(msg, true, text);
            switch (random.nextInt(1, 3)) {
                case 1: actions.setReaction(msg, "\uD83D\uDC4D"); break;
                case 2: actions.setReaction(msg, "\uD83D\uDC4E"); break;
            }
            return;
        }

        if (chatLimits.get(chatId) > 20 && random.nextInt(0, 5) == 0) {
            makeRandomAction(msg, false, text);
            chatLimits.put(chatId, 0);
        }
    }

    private void makeRandomAction(Message msg, boolean isReplyGuaranteed, String filteredText) {
        int randomNum = random.nextInt(0, 20);
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