package me.melontini.andromeda.util.commander;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.loader.api.FabricLoader;

public class CommanderSupport {

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("commander");

    public static void require(Module module) {
        if (module.meta().environment().isClient()) return;

        ConfigEvent.bootstrap(module).listen((moduleManager, config) -> {
            if (!LOADED && config.enabled)
                throw AndromedaException.builder().report(false)
                        .translatable("module_manager.requires_commander", "https://modrinth.com/project/cmd")
                        .build();
        });

        BlockadesEvent.BUS.listen((manager, blockade) -> blockade.explain(module, "enabled", (moduleManager) -> !LOADED, blockade.andromeda("missing_commander")));
    }
}
