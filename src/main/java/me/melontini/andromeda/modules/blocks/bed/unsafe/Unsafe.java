package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.base.util.ToBooleanFunction;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;

@ModuleInfo(name = "bed/unsafe", category = "blocks", environment = Environment.SERVER)
public class Unsafe extends Module<Module.BaseConfig> {

    Unsafe() {
        ToBooleanFunction<ModuleManager> supplier = (manager) -> manager.getDiscovered(Safe.class).map(Promise::get).filter(Module::enabled).isPresent();

        ConfigEvent.forModule(this).listen((moduleManager, manager) -> {
            manager.onSave((config, path) -> {
                if (supplier.getAsBoolean(moduleManager)) {
                    config.enabled = false;
                }
            });
        });
        BlockadesEvent.BUS.listen((manager, blockade) -> {
            blockade.explain(this, "enabled", supplier, blockade.andromeda("module_conflict"));
        });
    }
}
