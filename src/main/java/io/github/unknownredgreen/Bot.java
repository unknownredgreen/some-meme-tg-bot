package io.github.unknownredgreen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
class Bot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    @Getter
    private final List<String> data;
    private final Random random;
    private final int maxValue = 1000;


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        Message msg = update.getMessage();
        if (!msg.hasText()) return;
        long chatId = msg.getChatId();
        String text = msg.getText().replaceAll("\\R", " ");
        updateData(text);

        try {
            if (msg.getReplyToMessage() != null
                    &&
                msg.getReplyToMessage().getFrom().getId().equals(getMe().getId())
            ) {
                sendChangedMessage(chatId, msg);
                return;
            }
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }

        if (random.nextInt(0, 10) == 9) {
            sendChangedMessage(chatId, msg);
        }
    }

    private void sendChangedMessage(long chatId, Message msg) {
        if (random.nextInt(0, 10) == 9) {
            try {
                execute(SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text(getRandomGeneratedString())
                        .replyToMessageId(msg.getMessageId())
                        .build());
            } catch (TelegramApiException e) {
                log.warn(e.getMessage());
            }
        } else {
            try {
                execute(SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text(getRandomGeneratedString())
                        .build());
            } catch (TelegramApiException e) {
                log.warn(e.getMessage());
            }
        }
    }

    private void updateData(String str) {
        if (str.length() > 100) return;
        if (data.contains(str)) return;

        if (random.nextInt(0, 10) == 9) {
            str = str.replace(" ", "");
        }

        int size = data.size();

        if (size < maxValue) {
            data.add(str);
        } else if (size == maxValue) {
            data.set(random.nextInt(0, maxValue), str);
        } else {
            while (data.size() > maxValue) {
                data.removeLast();
            }
        }
    }

    private String getRandomGeneratedString() {
        String baseString1 = data.get(random.nextInt(0, data.size()));
        String baseString2 = data.get(random.nextInt(0, data.size()));

        StringBuilder sb = new StringBuilder();

        if (baseString1.length() < baseString2.length()) {
            sb.append(baseString1).append(" ").append(lowerFirstChar(baseString2));
        } else {
            sb.append(baseString2).append(" ").append(lowerFirstChar(baseString1));
        }

        return sb.toString();
    }

    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}