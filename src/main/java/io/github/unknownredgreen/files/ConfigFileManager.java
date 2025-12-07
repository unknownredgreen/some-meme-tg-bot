package io.github.unknownredgreen.files;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RequiredArgsConstructor
public class ConfigFileManager {
    private final String configFilePath;
    private List<String> startData;
    private final Map<String, String> map = new LinkedHashMap<>();
    private boolean canLoad = true;
    private boolean canSave = true;

    public void init() throws IOException {
        if (!canLoad) throw new IllegalStateException("Can`t load more than one time.");
        canLoad = false;
        startData = Files.readAllLines(Paths.get(configFilePath));
        for (String line : startData) {
            String[] parsedLine = line.split(":");
            String key = parsedLine[0];
            String value = parsedLine[1];
            map.put(key, value);
        }
    }

    public void fixSelf() throws IOException {
        if (!canSave) throw new IllegalStateException("Can`t save more than one time.");
        canSave = false;
        List<String> data = map.entrySet()
            .stream()
            .map(elem -> elem.getKey() + ":" + elem.getValue())
            .toList();
        if (data.equals(startData)) return;

        Files.write(Paths.get(configFilePath), data);
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
}
