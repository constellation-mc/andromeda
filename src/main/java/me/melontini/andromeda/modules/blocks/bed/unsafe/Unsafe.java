package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.minecraft.text.Text;

@OldConfigKey("bedsExplodeEverywhere")
@ModuleTooltip(2)
@ModuleInfo(name = "bed/unsafe", category = "blocks", environment = Environment.SERVER)
public class Unsafe extends BasicModule {

    @Override
    public void onConfig(ConfigManager<BasicConfig> manager) {
        manager.onSave(ConfigManager.State.PRE, config -> {
            if (ModuleManager.get().getDiscovered(Safe.class).filter(Module::enabled).isPresent()) {
                config.enabled = false;
            }
        });
    }

    @Override
    public void collectBlockades() {
        FeatureBlockade.get().explain(this, "enabled", () -> ModuleManager.get().getDiscovered(Safe.class).filter(Module::enabled).isPresent(),
                Text.translatable("andromeda.config.option_manager.reason.andromeda.module_conflict"));
    }
}
