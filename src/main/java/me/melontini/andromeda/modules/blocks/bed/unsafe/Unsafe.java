package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "bed/unsafe", category = "blocks", environment = Environment.SERVER)
public class Unsafe extends Module<Module.BaseConfig> {

    Unsafe() {
        BooleanSupplier supplier = () -> ModuleManager.get().getDiscovered(Safe.class).map(CompletableFuture::join).filter(Module::enabled).isPresent();

        ConfigEvent.forModule(this).listen(manager -> {
            manager.onSave((config, path) -> {
                if (supplier.getAsBoolean()) {
                    config.enabled = false;
                }
            });
        });
        BlockadesEvent.BUS.listen(blockade -> {
            blockade.explain(this, "enabled", supplier, blockade.andromeda("module_conflict"));
        });
    }
}
