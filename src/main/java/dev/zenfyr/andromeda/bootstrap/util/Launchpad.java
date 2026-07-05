package dev.zenfyr.andromeda.bootstrap.util;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.event.ModuleLoadStateEvent;
import dev.zenfyr.andromeda.bootstrap.util.mixin.AndromedaMixinPlugin;
import dev.zenfyr.pulsar.api.platform.Platform;
import java.io.IOException;

public class Launchpad {

  private static final boolean isLaunchpad;

  static {
    boolean launchpad = false;
    if (Platform.getPlatform().isModLoaded("neoforge")) {
      try {
        AndromedaMixinPlugin.getClassNode("org.sinytra.launchpad.api.Constants");
        launchpad = true;
      } catch (ClassNotFoundException | IOException e) {
        // NOOP
      }
    }
    isLaunchpad = launchpad;
  }

  public static boolean isLaunchpad() {
    return isLaunchpad;
  }

  public static void forModule(Module module) {
    if (isLaunchpad()) {
      ModuleLoadStateEvent.get(module)
          .listen(() -> ModuleLoadStateEvent.disable("not compatible with Launchpad!"));
    }
  }
}
