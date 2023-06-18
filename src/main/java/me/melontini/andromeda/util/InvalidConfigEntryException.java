package me.melontini.andromeda.util;

public class InvalidConfigEntryException extends RuntimeException {
    public InvalidConfigEntryException(String message) {
        super("[andromeda] " + message);
    }

    public InvalidConfigEntryException(String message, Throwable throwable) {
        super("[andromeda] " + message, throwable);
    }
}
