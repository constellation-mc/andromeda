package me.melontini.andromeda.util;

public class InvalidConfigEntryException extends RuntimeException {
    public InvalidConfigEntryException(String message) {
        super("(Andromeda) " + message);
    }

    public InvalidConfigEntryException(String message, Throwable throwable) {
        super("(Andromeda) " + message, throwable);
    }
}
