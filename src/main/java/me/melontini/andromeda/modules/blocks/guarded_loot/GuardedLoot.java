package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.common.util.TranslationKeyProvider;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Optional;

@ModuleInfo(name = "guarded_loot", category = "blocks")
public class GuardedLoot extends Module<GuardedLoot.Config> {

    GuardedLoot() {
    }

    public static class Config extends BaseConfig {
        public int range = 4;
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
