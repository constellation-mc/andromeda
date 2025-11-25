package dev.zenfyr.andromeda.common.config.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.EventMarker;

// Handler for main config files in /config/andromeda/
// The configs are split into multiple parts.
public class MultiConfigHandler extends AbstractConfigHandler {

  private final String state;

  public MultiConfigHandler(
      ModuleManager manager, Path path, String state, EventMarker<RegisterConfigEvent> id) {
    super(manager, path, id);
    this.state = state;
  }

  @Override
  protected Collection<Module> modules(ModuleManager manager) {
    return manager.all();
  }

  @Override
  protected void writeToFile(Path path, BaseConfig entry) throws IOException {
    JsonObject object;
    if (Files.exists(path)) {
      try (var reader = Files.newBufferedReader(path)) {
        object = JsonParser.parseReader(reader).getAsJsonObject();
      } catch (IOException | JsonParseException e) {
        object = new JsonObject();
      }
    } else {
      object = new JsonObject();
    }

    var o = this.gson.toJsonTree(entry).getAsJsonObject();
    if (!o.asMap().isEmpty()) {
      object.add(state, o);
      o.keySet().forEach(object::remove);
    }

    var parent = path.getParent();
    if (parent != null) Files.createDirectories(parent);
    Files.writeString(path, this.gson.toJson(object));
  }

  @Override
  protected BaseConfig readFromFile(Path path, ConfigDefinition<?> definition) throws IOException {
    try (var reader = Files.newBufferedReader(path)) {
      JsonElement element = JsonParser.parseReader(reader);
      if (!element.isJsonObject()) throw new IllegalStateException("Not a JsonObject!");

      JsonObject object = element.getAsJsonObject();
      object = Objects.requireNonNullElse(object.getAsJsonObject(state), object);
      return Objects.requireNonNull(
          this.gson.fromJson(object, definition.supplier().get()));
    }
  }
}
