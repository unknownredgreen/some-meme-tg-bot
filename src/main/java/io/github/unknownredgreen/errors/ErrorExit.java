package io.github.unknownredgreen.errors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ErrorExit {
    private static void exit(String message, int status) {
        log.error(message);
        System.exit(status);
    }

    public static void generic(String message) {
        exit(message, 1);
    }
    public static void config(String message) {
        exit(message, 2);
    }
}