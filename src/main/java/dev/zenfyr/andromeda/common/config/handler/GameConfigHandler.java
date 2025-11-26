package dev.zenfyr.andromeda.common.config.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.EventMarker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

public class GameConfigHandler extends AbstractConfigHandler {

  private final AbstractConfigHandler root;

  public GameConfigHandler(
      ModuleManager manager,
      AbstractConfigHandler root,
      Path path,
      EventMarker<RegisterConfigEvent> id) {
    super(manager, path, id);
    this.root = root;
  }

  @Override
  protected Collection<Module> modules(ModuleManager manager) {
    return manager.loaded();
  }

  @Override
  protected <T extends BaseConfig> T createDefault(ConfigDefinition<T> module) {
    return this.root.getDefault(module);
  }

  @Override
  protected void writeToFile(Path path, BaseConfig entry) throws IOException {
    JsonObject object = this.gson.toJsonTree(entry).getAsJsonObject();

    var parent = path.getParent();
    if (parent != null) Files.createDirectories(parent);
    Files.writeString(path, this.gson.toJson(object));
  }

  @Override
  protected <T extends BaseConfig> T createNewConfig(
      Module module, ConfigDefinition<T> definition) {
    if (this.root != null) {
      var cfg = root.get(definition);
      if (cfg != null) return (T) cfg.copy();
      return (T) root.load(module);
    }
    return super.createNewConfig(module, definition);
  }

  @Override
  protected BaseConfig readFromFile(Path path, ConfigDefinition<?> definition) throws IOException {
    try (var reader = Files.newBufferedReader(path)) {
      JsonElement element = JsonParser.parseReader(reader);
      if (!element.isJsonObject()) throw new IllegalStateException("Not a JsonObject!");

      JsonObject object = element.getAsJsonObject();
      return Objects.requireNonNull(
          this.gson.fromJson(object, definition.supplier().get()));
    }
  }
}
