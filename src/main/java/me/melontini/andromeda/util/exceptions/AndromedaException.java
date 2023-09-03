package me.melontini.andromeda.util.exceptions;

public class AndromedaException extends RuntimeException {

    private boolean report = true;

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

    public AndromedaException(boolean report) {
        super(buildMessage(report, ""));
        this.report = report;
    }

    public AndromedaException(boolean report, String message) {
        super(buildMessage(report, message));
        this.report = report;
    }

    public AndromedaException(boolean report, String message, Throwable cause) {
        super(buildMessage(report, message), cause);
        this.report = report;
    }

    public AndromedaException(boolean report, Throwable cause) {
        super(buildMessage(report, ""), cause);
        this.report = report;
    }

    public boolean shouldReport() {
        return report;
    }

    protected static String buildMessage(String message) {
        return buildMessage(true, message);
    }

    protected static String buildMessage(boolean report, String message) {
        return report ? "(Andromeda) " + message + "\n" + "If you have \"Send Crash Reports\" enabled this crash report would've been sent to the developer. Sorry! \n" : "(Andromeda) " + message;
    }
}
