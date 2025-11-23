package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "bed/unsafe", category = "blocks")
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
