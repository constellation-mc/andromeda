package dev.zenfyr.andromeda.common.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.zenfyr.pulsar.api.platform.Platform;
import dev.zenfyr.pulsar.api.util.Utilities;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {
  public static final Function<Screen, Screen> SCREEN_PROVIDER = Utilities.supply(() -> {
    if (Platform.getPlatform().isModLoaded("cloth-config")) {
      var builder = new AutoConfigScreen();
      return builder::getScreen;
    }
    return screen -> null;
  });

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return SCREEN_PROVIDER::apply;
  }
}
