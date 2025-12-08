package io.github.unknownredgreen;

import io.github.unknownredgreen.files.ConfigFileManager;
import io.github.unknownredgreen.files.SavedDataFileManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Main {
    private static SavedDataFileManager savedDataFileManager;
    private static ConfigFileManager configFileManager;
    private static final Random random = new Random();

    public static void main(String[] args) throws TelegramApiException, IOException {
        int neededArgCount = 4;
        String requiredArgsAdvice = """
                Required:
                1: bot username
                2: bot token
                3: data file path, like /home/mainuser/Desktop/telegram_bot/data.txt
                4: config file path, like /home/mainuser/Desktop/telegram_bot/config.txt
                """;

        if (args.length < neededArgCount) {
            throw new RuntimeException("""
                    Too few args
                    %s
                    """.formatted(requiredArgsAdvice));
        } else if (args.length > neededArgCount) {
            throw new RuntimeException("""
                    Too many args
                    %s
                    """.formatted(requiredArgsAdvice));
        }

        String botUsername = args[0];
        String botToken = args[1];
        String dataFilePath = args[2]; // like /home/mainuser/Desktop/telegram_bot/data.txt
        String configFilePath = args[3]; //like /home/mainuser/Desktop/telegram_bot/config.txt

        savedDataFileManager = new SavedDataFileManager(dataFilePath);
        configFileManager = new ConfigFileManager(configFilePath);
        configFileManager.init();

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot(botUsername, botToken, savedDataFileManager.load(), configFileManager, random);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                List<String> data = bot.getData();
                while (data.size() > bot.getMaxDataLength()) {
                    data.removeLast();
                }
                System.out.println("New data length: " + data.size());
                savedDataFileManager.save(data);
                configFileManager.fixSelf();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        botsApi.registerBot(bot);
    }
}