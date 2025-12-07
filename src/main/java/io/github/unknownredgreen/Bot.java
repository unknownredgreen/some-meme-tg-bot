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
    @Getter
    private final List<String> data;
    private final ConfigFileManager configFileManager;
    private final Random random;
    private final int maxValue = 1000;
    private String[] stickerIds;
    private boolean canSendStickers = true;
    @Override
    public void onRegister() {
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
        if (!msg.hasText()) return;
        long chatId = msg.getChatId();
        String text = msg.getText().replaceAll("\\R", " ");
        updateData(text);

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

        if (random.nextInt(0, 7) == 6) {
            try {
                makeRandomAction(chatId, msg);
            } catch (TelegramApiException e) {
                log.warn(e.getMessage());
            }
        }
    }

    private void makeRandomAction(long chatId, Message msg) throws TelegramApiException {
        class HelperClass {
            void sendRandomMessage() throws TelegramApiException {
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


            void sendRandomSticker() throws TelegramApiException {
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
        HelperClass localMethods = new HelperClass();

        switch (random.nextInt(1, 3)) {
            case 1: {
                localMethods.sendRandomMessage();
                break;
            }

            case 2: {
                if (canSendStickers) localMethods.sendRandomSticker();
                else localMethods.sendRandomMessage();
                break;
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