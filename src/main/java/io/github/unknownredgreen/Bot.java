package io.github.unknownredgreen;

import io.github.unknownredgreen.files.ConfigFileManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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
    private long botStartTimeInSeconds;
    @Getter
    private final List<String> data;
    private final ConfigFileManager configFileManager;
    private final Random random;
    @Getter
    private int maxDataLength;
    private String[] stickerIds;
    private boolean canSendStickers = true;
    private final RandomMessages randomMessages = new RandomMessages();

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
            maxDataLength = 1000;
        }
        try {
            stickerIds = configFileManager.parseStringArray("stickerIds");
        } catch (NullPointerException e) {
            canSendStickers = false;
        }
    }

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
        /*if (msg.hasSticker()) {
            System.out.println(msg.getSticker().getFileId());
        }*/
        if ((long) msg.getDate() < botStartTimeInSeconds) return;
        if (!msg.hasText()) return;
        long chatId = msg.getChatId();
        String text = msg.getText().replaceAll("\\R", " ");
        updateData(text);

        if (data.size() < 5) return;

        try {
            if (msg.getReplyToMessage() != null
                    &&
                msg.getReplyToMessage().getFrom().getId().equals(getMe().getId())
            ) {
                makeRandomAction(chatId, msg);
                return;
            }
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }

        if (random.nextInt(0, 20) == 0) {
            try {
                makeRandomAction(chatId, msg);
            } catch (TelegramApiException e) {
                log.warn(e.getMessage());
            }
        }
    }

    private class RandomMessages {
        public void sendRandomMessage(long chatId, Message msg) throws TelegramApiException {
            if (random.nextInt(0, 10) == 9) {
                execute(SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text(getRandomGeneratedString())
                        .replyToMessageId(msg.getMessageId())
                        .build());
            } else {
                execute(SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text(getRandomGeneratedString())
                        .build());
            }
        }


        public void sendRandomSticker(long chatId, Message msg) throws TelegramApiException {
            if (random.nextInt(0, 10) == 9) {
                execute(SendSticker.builder()
                        .sticker(new InputFile(stickerIds[random.nextInt(0, stickerIds.length)]))
                        .chatId(String.valueOf(chatId))
                        .replyToMessageId(msg.getMessageId())
                        .build());
            } else {
                execute(SendSticker.builder()
                        .sticker(new InputFile(stickerIds[random.nextInt(0, stickerIds.length)]))
                        .chatId(String.valueOf(chatId))
                        .build());
            }
        }
    }

    private void makeRandomAction(long chatId, Message msg) throws TelegramApiException {
        int randomNum = random.nextInt(0, 10);
        if (randomNum < 7) {
            randomMessages.sendRandomMessage(chatId, msg);
        } else {
            if (canSendStickers) randomMessages.sendRandomSticker(chatId, msg);
            else randomMessages.sendRandomMessage(chatId, msg);
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