package me.melontini.andromeda.util.exceptions;

import com.google.common.base.Strings;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.util.CrashHandler;
import me.melontini.dark_matter.api.analytics.Prop;
import me.melontini.dark_matter.api.base.util.classes.ThrowingRunnable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AndromedaException extends RuntimeException {

    private final boolean report;
    private final Map<String, String> statuses;
    private boolean appendStatuses = true;

    @SuppressWarnings("unused")
    private AndromedaException() {
        this(false, "Empty ctx called! This must never happen!!!", null, Collections.emptyMap());
    }

    private AndromedaException(boolean report, String message, Throwable cause, Map<String, String> statuses) {
        super(message, cause);
        this.report = report;
        this.statuses = statuses;
    }

    @Override
    public String getMessage() {
        return buildMessage(report, super.getMessage(), appendStatuses ? statuses : Collections.emptyMap());
    }

    public boolean shouldReport() {
        return report;
    }

    protected static String buildMessage(boolean report, String message, Map<String, String> statuses) {
        StringBuilder b = new StringBuilder();
        b.append("(Andromeda) ");
        if (Strings.isNullOrEmpty(message)) b.append("Something went very wrong!");
        else b.append(message);

        if (!statuses.isEmpty()) {
            var statusesList = statuses.entrySet().stream().toList();
            for (int i = 0; i < statusesList.size(); i += 2) {
                var e1 = statusesList.get(i);
                b.append("\n    ").append('\'').append(e1.getKey()).append("': ").append('\'').append(e1.getValue()).append("',    ");

                if (i + 1 < statusesList.size()) {
                    var e2 = statusesList.get(i + 1);
                    b.append('\'').append(e2.getKey()).append("': ").append('\'').append(e2.getValue()).append("'");
                }
            }
        }

        if (report)
            b.append('\n').append("If you have \"Send Crash Reports\" enabled this crash report would've been sent to the developer. Sorry!");
        return b.toString();
    }

    public static void run(ThrowingRunnable<Throwable> runnable, Supplier<Builder> builder) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw builder.get().cause(e).build();
        }
    }

    public static class Builder {
        private String message;
        private Throwable cause;
        private boolean report = true;

        private final Map<String, String> statuses = new LinkedHashMap<>();

        public Builder() {
            add("bootstrap_status", Bootstrap.getStatus());
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder report(boolean report) {
            this.report = report;
            return this;
        }

        public Builder add(Prop... props) {
            for (Prop prop : props) {
                statuses.put(prop.name().toLowerCase(), prop.get());
            }
            return this;
        }

        public Builder add(String key, Object value) {
            statuses.put(key, String.valueOf(value));
            return this;
        }

        private void disableInHierarchy(Throwable cause) {
            if (cause == null) return;
            if (cause instanceof AndromedaException e) {
                e.appendStatuses = false;
                e.statuses.forEach(this.statuses::putIfAbsent);
            }
            disableInHierarchy(cause.getCause());
        }

        public AndromedaException build() {
            return build(true);
        }

        public AndromedaException build(boolean submit) {
            disableInHierarchy(cause);

            var e = new AndromedaException(report,
                    Strings.isNullOrEmpty(message) ? "Something went very wrong!" : message,
                    cause, statuses);
            if (submit) CrashHandler.offer(e);
            return e;
        }
    }
}
