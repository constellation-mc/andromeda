package me.melontini.andromeda.util.exceptions;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.*;
import java.util.function.Consumer;
import lombok.CustomLog;
import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.EarlyLanguage;
import me.melontini.dark_matter.api.base.util.functions.ThrowingRunnable;
import me.melontini.dark_matter.api.crash_handler.Prop;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@CustomLog
@ToString
public final class AndromedaException extends RuntimeException {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final boolean report;
  private final JsonObject statuses;

  @ToString.Exclude
  private Consumer<StringBuilder> appender;

  private AndromedaException(
      boolean report, String message, @Nullable Throwable cause, JsonObject statuses) {
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

  public void setAppender(@Nullable Consumer<StringBuilder> b) {
    this.appender = b;
  }

  public JsonObject getStatuses() {
    return statuses.deepCopy();
  }

  public boolean shouldReport() {
    return report;
  }

  // referenced by name in MixinProcessor$Plugin#wrapNodeWithErrorHandling
  @SuppressWarnings("unused")
  public static AndromedaException moduleException(Throwable t, String module) {
    return AndromedaException.builder()
        .translatable("mixin_processor.handler_failed")
        .cause(t)
        .add("module", module)
        .build();
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

  public static void consume(ThrowingRunnable<Throwable> runnable, Consumer<Throwable> msg) {
    try {
      runnable.run();
    } catch (Throwable t) {
      msg.accept(t);
    }
  }

  public static String toString(JsonObject object) {
    return GSON.toJson(object);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final List<String> message = new ArrayList<>();

    @Nullable private Throwable cause;

    private boolean report = true;

    private final JsonObject statuses = new JsonObject();

    private Builder() {}

    public Builder translatable(String key, Object... args) {
      return this.literal(EarlyLanguage.translate("andromeda.exception." + key, args));
    }

    public Builder translatable(Module module, String key, Object... args) {
      return this.literal(EarlyLanguage.translate(
          "andromeda.%s.exception.%s".formatted(module.meta().dotted(), key), args));
    }

    public Builder literal(String message) {
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
      Arrays.stream(props)
          .forEach(prop -> this.add(prop.name().toLowerCase(Locale.ROOT), prop.get()));
      return this;
    }

    public Builder add(String key, Object value) {
      return this.add(key, String.valueOf(value));
    }

    public Builder add(String key, String value) {
      statuses.addProperty(key, value);
      return this;
    }

    public Builder add(String key, Object[] objArray) {
      return this.add(key, Arrays.asList(objArray));
    }

    public Builder add(String key, Collection<?> collection) {
      JsonArray array = new JsonArray();
      collection.stream().map(String::valueOf).forEach(array::add);
      statuses.add(key, array);
      return this;
    }

    public AndromedaException build() {
      return new AndromedaException(
          report,
          message.isEmpty()
              ? "Something went very wrong!"
              : StringUtils.join(message.toArray(), '\n'),
          cause,
          statuses);
    }
  }
}
