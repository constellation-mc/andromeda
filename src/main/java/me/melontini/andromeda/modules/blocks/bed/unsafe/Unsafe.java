package me.melontini.andromeda.modules.blocks.bed.unsafe;

import java.util.function.Predicate;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;

@ModuleInfo(name = "bed/unsafe", category = "blocks", environment = Environment.SERVER)
public final class Unsafe extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG =
      new ConfigDefinition<>(() -> Module.GameConfig.class);

  Unsafe() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    Predicate<ModuleManager> supplier = (manager) -> manager
        .getDiscovered(Safe.class)
        .map(Promise::get)
        .filter(safe -> manager.getConfig(safe).enabled())
        .isPresent();

    ConfigEvent.bootstrap(this).listen((moduleManager, config) -> {
      if (supplier.test(moduleManager)) {
        config.enabled = false;
      }
    });
    BlockadesEvent.BUS.listen((manager, blockade) -> {
      blockade.explain(this, "enabled", supplier, blockade.andromeda("module_conflict"));
    });
  }
}
