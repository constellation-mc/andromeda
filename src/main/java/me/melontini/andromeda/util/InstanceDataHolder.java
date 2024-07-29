package me.melontini.andromeda.util;

import com.google.gson.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.MakeSure;
import org.jetbrains.annotations.NotNull;

@CustomLog
@ToString
@RequiredArgsConstructor
public class InstanceDataHolder {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static Supplier<InstanceDataHolder> INSTANCE = () -> {
    throw new IllegalStateException("InstanceDataHolder requested too early!");
  };

  private final JsonObject data;

  public InstanceDataHolder putData(String key, JsonElement element) {
    synchronized (this) {
      this.data.add(key, element);
      return this;
    }
  }

  public InstanceDataHolder putData(String key, String value) {
    return this.putData(key, new JsonPrimitive(value));
  }

  public <T> InstanceDataHolder putData(
      String key,
      @NotNull Collection<? extends T> collection,
      Function<? super T, ? extends JsonElement> encoder) {
    if (collection.isEmpty()) return this;

    JsonArray array = new JsonArray();
    for (T t : collection) {
      array.add(encoder.apply(t));
    }
    return this.putData(key, array);
  }

  public void save() {
    synchronized (this) {
      try (var writer =
          Files.newBufferedWriter(CommonValues.hiddenPath().resolve("instance_data.json"))) {
        GSON.toJson(this.data, writer);
      } catch (IOException e) {
        throw AndromedaException.builder()
            .literal("Failed to save instance data!")
            .report(false)
            .cause(e)
            .build();
      }
    }
  }

  public void modifyAndSave(@NotNull Runnable consumer) {
    consumer.run();
    this.save();
  }

  public @NotNull JsonElement getData(String string) {
    return MakeSure.notNull(this.data.get(string), string);
  }

  public boolean hasData(String string) {
    return this.data.has(string);
  }

  public static void load() {
    JsonObject holder = new JsonObject();
    Path path = CommonValues.hiddenPath().resolve("instance_data.json");

    if (Files.exists(path)) {
      try (var reader = Files.newBufferedReader(path)) {
        holder = GSON.fromJson(reader, JsonObject.class).getAsJsonObject();
      } catch (IOException | JsonParseException e) {
        LOGGER.error("Failed to load instance data! resetting to default...", e);
      }
    }

    var realDeal = new InstanceDataHolder(holder);
    INSTANCE = () -> realDeal;
  }

  public static InstanceDataHolder get() {
    return INSTANCE.get();
  }
}
