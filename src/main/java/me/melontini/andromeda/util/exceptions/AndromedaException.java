package me.melontini.andromeda.util.exceptions;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.CustomLog;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.classes.ThrowingRunnable;
import me.melontini.dark_matter.api.crash_handler.Prop;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@CustomLog
public class AndromedaException extends RuntimeException {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final boolean report;
    private final JsonObject statuses;
    private Consumer<StringBuilder> appender;

    @SuppressWarnings("unused")
    private AndromedaException() {
        this(false, "Empty ctx called! This must never happen!!!", null, new JsonObject());
    }

    private AndromedaException(boolean report, String message, Throwable cause, JsonObject statuses) {
        super(message, cause);
        this.report = report;
        this.statuses = statuses;
    }

    @Override
    public String getMessage() {
        StringBuilder b = new StringBuilder();
        b.append("(Andromeda) ");
        if (Strings.isNullOrEmpty(super.getMessage())) b.append("Something went very wrong!");
        else b.append(super.getMessage());
        if (appender != null) appender.accept(b.append('\n'));
        return b.toString();
    }

    public void setAppender(Consumer<StringBuilder> b) {
        this.appender = b;
    }

    public JsonObject getStatuses() {
        return statuses.deepCopy();
    }

    public boolean shouldReport() {
        return report;
    }

    //referenced by name in MixinProcessor$Plugin#wrapNodeWithErrorHandling
    @SuppressWarnings("unused")
    public static AndromedaException moduleException(Throwable t, String module) {
        return AndromedaException.builder()
                .message("Andromeda module caught a mixin handler exception! There's no guarantee that this is Andromeda's fault.")
                .cause(t).add("module", module).build();
    }

    public static void run(ThrowingRunnable<Throwable> runnable, Consumer<Builder> consumer) {
        try {
            runnable.run();
        } catch (Throwable e) {
            var builder = AndromedaException.builder();
            consumer.accept(builder);
            throw builder.cause(e).build();
        }
    }

    public static String toString(JsonObject object) {
        return GSON.toJson(object);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static JsonObject defaultStatuses() {
        return new Builder().statuses;
    }

    public static class Builder {

        private static final Map<String, Consumer<Builder>> DEFAULT_KEYS = Map.of(
                "bootstrap_status", b -> b.add("bootstrap_status", Bootstrap.Status.get()),
                "platform", b -> b.add("platform", CommonValues.platform()),
                prop(Prop.ENVIRONMENT), b -> b.add(Prop.ENVIRONMENT),
                prop(Prop.OS), b -> b.add(Prop.OS),
                prop(Prop.JAVA_VERSION), b -> b.add(Prop.JAVA_VERSION),
                prop(Prop.JAVA_VENDOR), b -> b.add(Prop.JAVA_VENDOR)
        );

        private static String prop(Prop prop) {
            return prop.name().toLowerCase();
        }

        private final List<String> message = new ArrayList<>();
        private Throwable cause;
        private boolean report = true;

        private final JsonObject statuses = new JsonObject();

        private Builder() {
            DEFAULT_KEYS.values().forEach(c -> c.accept(this));
        }

        public Builder message(String message) {
            this.message.add(message);
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
                statuses.addProperty(prop.name().toLowerCase(), prop.get());
            }
            return this;
        }

        public Builder add(String key, Object value) {
            statuses.addProperty(key, String.valueOf(value));
            return this;
        }

        public Builder add(String key, String value) {
            statuses.addProperty(key, value);
            return this;
        }

        public Builder add(String key, Collection<?> collection) {
            JsonArray array = new JsonArray();
            for (Object object : collection) {
                array.add(GSON.toJsonTree(object));
            }
            statuses.add(key, array);
            return this;
        }

        private void disableInHierarchy(Throwable cause) {
            if (cause == null) return;
            if (cause instanceof AndromedaException e) {
                for (String defaultKey : DEFAULT_KEYS.keySet()) {
                    e.statuses.remove(defaultKey);
                }
            }
            disableInHierarchy(cause.getCause());
        }

        public AndromedaException build() {
            disableInHierarchy(cause);

            //CrashHandler can't automatically handle preLaunch errors, so this is what we have to do.
            return new AndromedaException(report,
                    message.isEmpty() ? "Something went very wrong!" : StringUtils.join(message.toArray(), '\n'),
                    cause, statuses);
        }
    }
}
