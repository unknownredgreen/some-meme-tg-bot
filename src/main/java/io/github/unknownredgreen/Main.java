package io.github.unknownredgreen;

import io.github.unknownredgreen.bot.Bot;
import io.github.unknownredgreen.errors.ErrorExit;
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
public final class Main {
    private static final int NEEDED_ARG_COUNT = 4;
    private static final int MAX_OPTIONAL_ARG_COUNT = 1;

    public static void main(String[] args) {
        try {
            // ensure args are right, and file exist
            prepareArgsAndFiles(args);
            // launch
            launchBot(args);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private static void prepareArgsAndFiles(String[] args) {
        String requiredArgsAdvice = """
                Required:
                1: bot username
                2: bot token
                3: data file path, like /home/mainuser/Desktop/telegram_bot/data.txt
                4: config file path, like /home/mainuser/Desktop/telegram_bot/config.txt
                Optional:
                5: -c | --create (to create the directories and the files specified in file path arguments if not found)
                """;

        if (args.length < NEEDED_ARG_COUNT) {
            ErrorExit.generic("""
                    Too few args
                    %s
                    """.formatted(requiredArgsAdvice)
            );
            System.exit(1);
        } else if (args.length > NEEDED_ARG_COUNT + MAX_OPTIONAL_ARG_COUNT) {
            ErrorExit.generic("""
                    Too many args
                    %s
                    """.formatted(requiredArgsAdvice)
            );
        }

        String dataFilePath = args[2];
        String configFilePath = args[3];

        boolean createNotFoundFiles = false;

        for (int i = 4; i < args.length; i++) {
            switch (args[i]) {
                case "-c", "--create": createNotFoundFiles = true; break;

                default: {
                    ErrorExit.generic("No such option '%s'".formatted(args[i]));
                }
            }
        }
        try {
            ensureFilesExist(createNotFoundFiles, dataFilePath, configFilePath);
        } catch (IOException e) {
            ErrorExit.generic(e.getMessage());
        }
    }

    private static void launchBot(String[] args) throws TelegramApiException {
        String botUsername = args[0].replace("@", "");
        String botToken = args[1];
        String dataFilePath = args[2];
        String configFilePath = args[3];

        Random random = new Random();
        final SavedDataFileManager savedDataFileManager = new SavedDataFileManager(dataFilePath);
        final ConfigFileManager configFileManager = new ConfigFileManager(configFilePath);
        configFileManager.init();
        final ConfigStorage configStorage = new ConfigStorage(configFileManager);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        EveryHourStatsLog everyHourStatsLog = new EveryHourStatsLog();
        Bot bot = new Bot(botUsername, botToken, savedDataFileManager.load(), configStorage, random, everyHourStatsLog);

        everyHourStatsLog.startLogging();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            List<String> data = bot.getData();
            while (data.size() > configStorage.getMaxDataLength()) {
                data.removeLast();
            }
            log.info("New data length: " + data.size());
            savedDataFileManager.save(data);
        }));
        botsApi.registerBot(bot);
    }

    private static void ensureFilesExist(boolean createNotFoundFiles, String... filePathsStrs) throws IOException {
        for (String filePathStr : filePathsStrs) {
            Path path = null;
            try {
                path = Path.of(filePathStr);
            } catch (InvalidPathException e) {
                ErrorExit.generic("Path %s is invalid".formatted(filePathStr));
            }

            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    ErrorExit.generic("%s is a directory and not a file".formatted(filePathStr));
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
                    ErrorExit.generic("File at path %s does not exist (use --create to create files that weren`t found)"
                            .formatted(filePathStr)
                    );
                }
            }
        }
    }
}