package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.common.util.TranslationKeyProvider;
import me.melontini.andromeda.util.commander.NumberIntermediary;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;
import java.util.Optional;

@ModuleInfo(name = "guarded_loot", category = "blocks")
public class GuardedLoot extends Module<GuardedLoot.Config> {

    GuardedLoot() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    public static class Config extends BaseConfig {
        public NumberIntermediary range = NumberIntermediary.of(4);
        public boolean allowLockPicking = true;
        @ConfigEntry.Gui.EnumHandler
        public BreakingHandler breakingHandler = BreakingHandler.UNBREAKABLE;
    }

    public enum BreakingHandler implements TranslationKeyProvider {
        NONE,
        UNBREAKABLE;

        @Override
        public Optional<String> getTranslationKey() {
            return Optional.of("config.andromeda.blocks.guarded_loot.option.BreakingHandler." + name());
        }
    }
}
