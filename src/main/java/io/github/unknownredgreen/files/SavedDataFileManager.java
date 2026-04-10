package io.github.unknownredgreen.files;

import io.github.unknownredgreen.errors.ErrorExit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class SavedDataFileManager {
    private final String dataFilePath;

    private boolean canLoad = true;
    private boolean canSave = true;

    public List<String> load() {
        if (!canLoad) throw new IllegalStateException("Can`t load more than one time.");
        canLoad = false;
        try {
            return Files.readAllLines(Paths.get(dataFilePath));
        } catch (IOException e) {
            ErrorExit.generic(e.getMessage());
        }
        // will not return null, only will exit if loading failed
        return null;
    }

    public void save(List<String> data) {
        if (!canSave) throw new IllegalStateException("Can`t save more than one time.");
        canSave = false;
        try {
            Files.write(Paths.get(dataFilePath), data);
        } catch (IOException e) {
            //data saving is performed only at exit rn, so not ErrorExit
            log.error(e.getMessage());
        }
    }
}
