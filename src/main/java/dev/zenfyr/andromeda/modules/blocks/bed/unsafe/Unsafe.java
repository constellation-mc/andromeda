package dev.zenfyr.andromeda.modules.blocks.bed.unsafe;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "bed/unsafe", category = "blocks", env = Environment.SERVER)
public final class Unsafe extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  Unsafe() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
    // TODO This is extremely unreliable. Should be its own event maybeeeee
    // Predicate<ModuleManager> supplier = (manager) -> manager
    //    .getDiscovered(Safe.class)
    //    .map(Promise::get)
    //    .filter(safe -> manager.getConfig(safe).enabled)
    //    .isPresent();
    //
    // ConfigEvent.bootstrap(this).listen((moduleManager, config) -> {
    //  if (supplier.test(moduleManager)) {
    //    config.enabled = false;
    //  }
    // });
    // BlockadesEvent.BUS.listen((manager, blockade) -> {
    //  blockade.explain(this, "enabled", supplier, blockade.andromeda("module_conflict"));
    // });
  }
}
