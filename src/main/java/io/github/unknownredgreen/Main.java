package io.github.unknownredgreen;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.Random;

public class Main {
    private static FileManager fileManager;
    private static final Random random = new Random();

    public static void main(String[] args) throws TelegramApiException, IOException {
        if (args.length < 3) {
            throw new RuntimeException("""
                    Too few args
                    Required:
                    1: bot username
                    2: bot token
                    3: data file path, like /home/mainuser/Desktop/telegram_bot/data.txt
                    """);
        } else if (args.length > 3) {
            throw new RuntimeException("""
                    Too many args
                    Required:
                    1: bot username
                    2: bot token
                    3: data file path, like /home/mainuser/Desktop/telegram_bot/data.txt
                    """);
        }

        String botUsername = args[0];
        String botToken = args[1];
        String dataFilePath = args[2]; // like /home/mainuser/Desktop/telegram_bot/data.txt

        fileManager = new FileManager(dataFilePath);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot(botUsername, botToken, fileManager.load(), random);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                fileManager.save(bot.getData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        botsApi.registerBot(bot);
    }
}