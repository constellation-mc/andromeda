package dev.zenfyr.andromeda.bootstrap;

import dev.zenfyr.andromeda.bootstrap.event.EventMarker;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.event.bus.Bus;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class Module {

  private final ModuleInfo meta;
  private final Supplier<Logger> logger =
      Memoize.supplier(() -> LogManager.getLogger(ModuleHelper.id(this)));
  private final Map<EventMarker<?>, Bus<?>> busMap = new IdentityHashMap<>();

  protected Module() {
    this.meta = this.getClass().getAnnotation(ModuleInfo.class);

    // This event is extremely common, so it's fine auto-subscribe
    if (this instanceof PostBootstrapEvent pbe) PostBootstrapEvent.get(this).listen(pbe);
  }

  public <E> Bus<E> getOrCreateBus(EventMarker<E> id, @Nullable Supplier<Bus<E>> supplier) {
    return (Bus<E>) busMap.computeIfAbsent(id, aClass -> supplier == null ? null : supplier.get());
  }

  public void dropBus(EventMarker<?> id) {
    busMap.remove(id);
  }

  public Logger logger() {
    return this.logger.get();
  }

  public ModuleInfo meta() {
    return this.meta;
  }
}
