package me.melontini.andromeda.util.commander;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.loader.api.FabricLoader;

public class CommanderSupport {

  private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("commander");
  private static final Set<Module> REQUIRE = new HashSet<>();
  public static final Predicate<Module> PREDICATE = module -> !REQUIRE.contains(module);

  public static void require(Module module) {
    if (module.meta().environment().isClient()) return;
    if (LOADED) return;
    REQUIRE.add(module);

    ConfigEvent.bootstrap(module).listen((moduleManager, config) -> {
      if (config.enabled)
        throw AndromedaException.builder()
            .report(false)
            .translatable("module_manager.requires_commander", "https://modrinth.com/project/cmd")
            .build();
    });

    BlockadesEvent.BUS.listen((manager, blockade) -> blockade.explain(
        module, "enabled", (moduleManager) -> true, blockade.andromeda("missing_commander")));
  }
}
