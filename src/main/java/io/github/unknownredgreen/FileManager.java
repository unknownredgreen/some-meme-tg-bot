package io.github.unknownredgreen;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
class FileManager {
    private final String dataFilePath;

    private boolean canLoad = true;
    private boolean canSave = true;

    public List<String> load() throws IOException {
        if (!canLoad) throw new IllegalStateException("Can`t load more than one time.");
        canLoad = false;
        return Files.readAllLines(Paths.get(dataFilePath));
    }

    public void save(List<String> data) throws IOException {
        if (!canSave) throw new IllegalStateException("Can`t save more than one time.");
        canSave = false;
        Files.write(Paths.get(dataFilePath), data);
    }
}
