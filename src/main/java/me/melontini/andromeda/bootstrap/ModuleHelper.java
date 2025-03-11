package me.melontini.andromeda.bootstrap;

import java.util.function.Consumer;
import me.melontini.andromeda.bootstrap.event.EventMarker;

public class ModuleHelper {

  public static <T> void runAndDropBus(Module module, EventMarker<T> marker, Consumer<T> invoker) {
    var bus = module.getOrCreateBus(marker, null);
    if (bus != null) invoker.accept(bus.invoker());
    module.dropBus(marker);
  }

  public static String mixinPackage(Module module) {
    return module.getClass().getPackageName() + ".mixin";
  }

  public static ModuleInfo getMeta(Class<?> cls) {
    return cls.getAnnotation(ModuleInfo.class);
  }

  public static String dotted(ModuleInfo info) {
    return id(info).replace('/', '.');
  }

  public static String dotted(Module info) {
    return dotted(info.meta());
  }

  public static String id(ModuleInfo info) {
    return info.category() + '/' + info.name();
  }

  public static String id(Module info) {
    return id(info.meta());
  }
}
