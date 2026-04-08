package io.github.unknownredgreen.bot;

import io.github.unknownredgreen.ConfigStorage;
import io.github.unknownredgreen.EveryHourStatsLog;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
final class BotActions {
    private final Bot bot;
    private final Random random;
    private final List<String> data;
    private final EveryHourStatsLog everyHourStatsLog;
    private final String[] stickerIds;
    private final String[] reactionEmojis;

    public BotActions(Bot bot, Random random, List<String> data, ConfigStorage configStorage, EveryHourStatsLog everyHourStatsLog) {
        this.bot = bot;
        this.random = random;
        this.data = data;
        this.everyHourStatsLog = everyHourStatsLog;

        stickerIds = configStorage.getStickerIds();
        reactionEmojis = configStorage.getReactionEmojis();
    }

    public void sendRandomMessage(Message msg, boolean isReplyGuaranteed, String filteredText) {
        everyHourStatsLog.incrementIntValue("Messages sent");
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(String.valueOf(msg.getChatId()))
                .text(getRandomGeneratedString(filteredText));

        if (isReplyGuaranteed || (random.nextInt(10) == 0)) {
            builder.replyToMessageId(msg.getMessageId());
        }
        try {
            bot.execute(builder.build());
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
    public void sendRandomSticker(Message msg) {
        everyHourStatsLog.incrementIntValue("Stickers sent");
        try {
            bot.execute(SendSticker.builder()
                    .sticker(new InputFile(stickerIds[random.nextInt(stickerIds.length)]))
                    .chatId(String.valueOf(msg.getChatId()))
                    .replyToMessageId(msg.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
    public void setRandomReaction(Message msg) {
        setReaction(msg, reactionEmojis[random.nextInt(reactionEmojis.length)]);
    }
    public void setReaction(Message msg, String reaction) {
        everyHourStatsLog.incrementIntValue("Reactions set to messages");
        List<ReactionType> emoji = new ArrayList<>();
        emoji.add(new ReactionTypeEmoji("emoji", reaction));
        try {
            bot.execute(SetMessageReaction.builder()
                    .chatId(msg.getChatId())
                    .messageId(msg.getMessageId())
                    .reactionTypes(emoji)
                    .build());
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private String getRandomGeneratedString(String textToInclude) {
        List<String> strings = new ArrayList<>();

        for (int i = 0; i < random.nextInt(5, 11); i++) {
            strings.add(data.get(random.nextInt(data.size())));
        }
        strings.add(
                random.nextInt(strings.size()+1),
                textToInclude
        );

        StringBuilder sb = new StringBuilder(strings.getFirst());

        sb.append(" ").append(lowerFirstChar(strings.get(1))).append(" ");

        for (int i = 2; i < strings.size(); i++) {
            String[] split = strings.get(i).split(" ");
            String appendable = split[random.nextInt(split.length)];
            boolean upped = false;
            if (random.nextInt(2) == 0) {
                appendable = appendable.toUpperCase();
                upped = true;
            } else if (!appendable.equals(appendable.toUpperCase())) {
                appendable = lowerFirstChar(appendable);
            }
            sb.append(appendable);
            if (upped) {
                switch (random.nextInt(3)) {
                    case 1: sb.append("!!!!!"); break;
                    case 2: sb.append("?"); break;
                }
            }
            if (random.nextInt(10) != 0) sb.append(" ");
        }

        String finalString = sb.toString();
        switch (random.nextInt(2)) {
            case 0: finalString = finalString.replaceAll("\\d+", String.valueOf(random.nextInt(4))); break; //replace all nums with 0-3
            case 1: finalString = finalString.replaceAll("\\d+", String.valueOf(random.nextInt(99999999))); break; //replace all nums with 0-99999998
        }

        return finalString;
    }

    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}