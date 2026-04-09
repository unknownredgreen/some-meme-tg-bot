package io.github.unknownredgreen;

import io.github.unknownredgreen.bot.Bot;
import io.github.unknownredgreen.files.ConfigFileManager;
import io.github.unknownredgreen.files.SavedDataFileManager;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

@Slf4j
public class Main {
    private static SavedDataFileManager savedDataFileManager;
    private static ConfigFileManager configFileManager;
    private static final Random random = new Random();
    private static ConfigStorage configStorage;

    public static void main(String[] args) throws TelegramApiException, IOException {
        int neededArgCount = 4;
        int maxOptionalArgCount = 1;
        String requiredArgsAdvice = """
                Required:
                1: bot username
                2: bot token
                3: data file path, like /home/mainuser/Desktop/telegram_bot/data.txt
                4: config file path, like /home/mainuser/Desktop/telegram_bot/config.txt
                Optional:
                5: -c | --create (to create the directories and the files specified in file path arguments if not found)
                """;

        if (args.length < neededArgCount) {
            throw new RuntimeException("""
                    Too few args
                    %s
                    """.formatted(requiredArgsAdvice));
        } else if (args.length > neededArgCount + maxOptionalArgCount) {
            throw new RuntimeException("""
                    Too many args
                    %s
                    """.formatted(requiredArgsAdvice));
        }

        String botUsername = args[0];
        String botToken = args[1];
        String dataFilePath = args[2]; // like /home/mainuser/Desktop/telegram_bot/data.txt
        String configFilePath = args[3]; //like /home/mainuser/Desktop/telegram_bot/config.txt

        boolean createNotFoundFiles = false;

        for (int i = 4; i < args.length; i++) {
            switch (args[i]) {
                case "-c", "--create": createNotFoundFiles = true; break;

                default: throw new IllegalArgumentException("No such option '%s'".formatted(args[i]));
            }
        }

        ensureFilesExist(createNotFoundFiles, dataFilePath, configFilePath);

        savedDataFileManager = new SavedDataFileManager(dataFilePath);
        configFileManager = new ConfigFileManager(configFilePath);
        configFileManager.init();
        configStorage = new ConfigStorage(configFileManager);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        EveryHourStatsLog everyHourStatsLog = new EveryHourStatsLog();
        Bot bot = new Bot(botUsername, botToken, savedDataFileManager.load(), configStorage, random, everyHourStatsLog);

        everyHourStatsLog.startLogging();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                List<String> data = bot.getData();
                while (data.size() > configStorage.getMaxDataLength()) {
                    data.removeLast();
                }
                log.info("New data length: " + data.size());
                savedDataFileManager.save(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        botsApi.registerBot(bot);
    }

    private static void ensureFilesExist(boolean createNotFoundFiles, String... filePathsStrs) throws IOException {
        for (String filePathStr : filePathsStrs) {
            Path path;
            try {
                path = Path.of(filePathStr);
            } catch (InvalidPathException e) {
                throw new RuntimeException("Path %s is invalid".formatted(filePathStr));
            }

            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    throw new RuntimeException("%s is a directory and not a file".formatted(filePathStr));
                }
            } else {
                if (createNotFoundFiles) {
                    Path parentPath = path.getParent();
                    if (parentPath != null && !Files.exists(parentPath)) {
                        Files.createDirectories(parentPath);
                        log.info("Created needeed directories {}", parentPath);
                    }
                    Files.createFile(path);
                    log.info("Created file {}", path);
                } else {
                    throw new RuntimeException("File at path %s does not exist (use --create to create files that weren`t found)"
                            .formatted(filePathStr)
                    );
                }
            }
        }
    }
}