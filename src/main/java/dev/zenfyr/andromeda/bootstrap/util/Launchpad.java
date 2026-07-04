package dev.zenfyr.andromeda.bootstrap.util;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.event.ModuleLoadStateEvent;

public class Launchpad {

  private static final boolean isLaunchpad;

  static {
    boolean launchpad = false;
    try {
      Class.forName("org.sinytra.launchpad.api.Constants");
      launchpad = true;
    } catch (ClassNotFoundException e) {
      // NOOP
    }
    isLaunchpad = launchpad;
  }

  public static boolean isLaunchpad() {
    return isLaunchpad;
  }

  public static void forModule(Module module) {
    if (isLaunchpad()) {
      ModuleLoadStateEvent.get(module)
          .listen(() -> new ModuleLoadStateEvent.Result(
              ModuleLoadStateEvent.ForcedState.DISABLE, "not compatible with Launchpad!"));
    }
  }
}
