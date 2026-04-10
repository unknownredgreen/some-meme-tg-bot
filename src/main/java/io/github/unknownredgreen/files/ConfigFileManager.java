package io.github.unknownredgreen.files;

import io.github.unknownredgreen.errors.ErrorExit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RequiredArgsConstructor
public final class ConfigFileManager {
    @Getter
    private final String configFilePath;
    private List<String> startData;
    private final Map<String, String> map = new LinkedHashMap<>();
    private boolean canLoad = true;

    public void init() {
        if (!canLoad) throw new IllegalStateException("Can`t load more than one time.");
        canLoad = false;
        try {
            startData = Files.readAllLines(Paths.get(configFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String line : startData) {
            String[] parsedLine = line.split(":");
            String key = parsedLine[0];
            if (parsedLine.length != 2) ErrorExit.config("Wrong key:value inside config at %s".formatted(key));
            String value = parsedLine[1];
            map.put(key, value);
        }
    }

    public String parseString(String key) {
        return map.get(key);
    }
    public int parseInt(String key) {
        return Integer.parseInt(parseString(key));
    }
    public String[] parseStringArray(String key) {
        return map.get(key).split(",");
    }
    public int[] parseIntArray(String key) {
        return Arrays.stream(
                parseStringArray(key)
        )
        .mapToInt(Integer::parseInt)
        .toArray();
    }
    public Boolean parseBoolean(String key) {
        String value = parseString(key);
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            ErrorExit.config("Invalid boolean value for key '%s' in config".formatted(key));
        }
        return Boolean.parseBoolean(value);
    }
    public Map<String, String> parseMapStringString(String key) {
        Map<String, String> finalMap = new HashMap<>();
        for (String str : parseStringArray(key)) {
            String[] split = str.split("=");
            if (split.length > 2) ErrorExit.config("Config: Too much arguments for map inside key '%s'".formatted(key));
            if (split.length < 2) ErrorExit.config("Config: Not enough arguments for map inside key '%s'".formatted(key));
            finalMap.put(split[0], split[1]);
        }
        return finalMap;
    }
}
