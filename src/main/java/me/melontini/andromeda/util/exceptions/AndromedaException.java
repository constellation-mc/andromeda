package me.melontini.andromeda.util.exceptions;

public class AndromedaException extends RuntimeException {
    public AndromedaException() {
        super(buildMessage(""));
    }

    public AndromedaException(String message) {
        super(buildMessage(message));
    }

    public AndromedaException(String message, Throwable cause) {
        super(buildMessage(message), cause);
    }

    public AndromedaException(Throwable cause) {
        super(buildMessage(""), cause);
    }

    protected AndromedaException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(buildMessage(message), cause, enableSuppression, writableStackTrace);
    }

    protected static String buildMessage(String message) {
        return "(Andromeda) " + message + "\n" + "If you have \"Send Crash Reports\" enabled this crash report would've been sent to the developer. Sorry! \n";
    }
}
