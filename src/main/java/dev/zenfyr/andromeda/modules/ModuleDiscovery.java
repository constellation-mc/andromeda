package dev.zenfyr.andromeda.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.pulsar.api.util.MakeSure;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ModuleDiscovery {

  @SuppressWarnings("unchecked")
  public List<Class<? extends Module>> discoverModules() {
    try (var stream =
        ModuleDiscovery.class.getClassLoader().getResourceAsStream("andromeda_modules.json")) {
      MakeSure.notNull(stream, "Required andromeda_modules.json file not found!");

      JsonArray array = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonArray();
      return (List<Class<? extends Module>>) (List<?>) array.asList().stream()
          .map(element -> {
            try {
              return Class.forName(element.getAsString());
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          })
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
