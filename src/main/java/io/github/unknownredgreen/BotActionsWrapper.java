package io.github.unknownredgreen;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
final class BotActionsWrapper {
    private final Bot bot;
    private final Random random;
    private final List<String> data = bot.getData();
    private final String[] stickerIds = bot.getStickerIds();

    public void sendRandomMessage(Message msg, boolean isReplyGuaranteed) throws TelegramApiException {
        if (isReplyGuaranteed) {
            bot.execute(SendMessage.builder()
                    .chatId(String.valueOf(msg.getChatId()))
                    .text(getRandomGeneratedString())
                    .replyToMessageId(msg.getMessageId())
                    .build());
            return;
        }

        if (random.nextInt(0, 10) == 0) {
            bot.execute(SendMessage.builder()
                    .chatId(String.valueOf(msg.getChatId()))
                    .text(getRandomGeneratedString())
                    .replyToMessageId(msg.getMessageId())
                    .build());
        } else {
            bot.execute(SendMessage.builder()
                    .chatId(String.valueOf(msg.getChatId()))
                    .text(getRandomGeneratedString())
                    .build());
        }
    }
    public void sendRandomSticker(Message msg) throws TelegramApiException {
        bot.execute(SendSticker.builder()
                .sticker(new InputFile(stickerIds[random.nextInt(0, stickerIds.length)]))
                .chatId(String.valueOf(msg.getChatId()))
                .replyToMessageId(msg.getMessageId())
                .build());
    }

    private String getRandomGeneratedString() {
        String baseString1 = data.get(random.nextInt(0, data.size()));
        String baseString2 = data.get(random.nextInt(0, data.size()));

        if (random.nextInt(0, 10) == 0) {
            baseString2 = new StringBuilder(baseString2).reverse().toString();
        }

        StringBuilder sb = new StringBuilder();

        if (baseString1.codePointCount(0, baseString1.length()) < baseString2.codePointCount(0, baseString2.length())) {
            sb.append(baseString1).append(" ").append(lowerFirstChar(baseString2));
        } else {
            sb.append(baseString2).append(" ").append(lowerFirstChar(baseString1));
        }

        if (sb.codePointCount(0, sb.length())< 30) {
            while (sb.codePointCount(0, sb.length()) < 50) {
                String randomStr = data.get(random.nextInt(0, data.size()));
                sb.append(randomStr);
            }
        }

        switch (random.nextInt(0, 5)) {
            case 1: {
                for (int i = 0; i < 7; i++) {
                    sb.deleteCharAt(i);
                }
                break;
            }
            case 2: {
                sb.insert(
                        random.nextInt(0, sb.codePointCount(0, sb.length())),
                        String.valueOf(random.nextInt(0, 10))
                                .repeat(random.nextInt(1, 4))
                );
                break;
            }
            case 3: {
                for (int i = 0; i < random.nextInt(1, 4); i++) {
                    String randomData = data.get(random.nextInt(0, data.size()));
                    String[] split = randomData.split(" ");
                    sb.insert(sb.indexOf(" "), sb.append(split[random.nextInt(0, split.length)])).append(" ");
                }
            }
        }
        return sb.toString();
    }

    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}