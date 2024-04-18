package me.melontini.andromeda.util;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.loader.api.FabricLoader;

public class CommanderSupport {

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("commander");

    public static void require(Module<?> module) {
        if (module.meta().environment().isClient()) return;

        ConfigEvent.forModule(module).listen(manager -> {
            manager.onLoad((config, path) -> {
                if (!LOADED && config.enabled)
                    throw AndromedaException.builder().report(false)
                            .translatable("module_manager.requires_commander", "https://modrinth.com/project/cmd")
                            .build();
            });
            manager.onSave((config, path) -> {
                if (!LOADED && config.enabled)
                    throw AndromedaException.builder().report(false)
                            .translatable("module_manager.requires_commander", "https://modrinth.com/project/cmd")
                        .build();
            });
        });

        BlockadesEvent.BUS.listen(blockade -> blockade.explain(module, "enabled", () -> !LOADED, blockade.andromeda("missing_commander")));
    }
}
