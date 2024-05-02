package me.melontini.andromeda.modules.blocks.guarded_loot;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.common.util.TranslationKeyProvider;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;

import java.util.List;
import java.util.Optional;

@ModuleInfo(name = "guarded_loot", category = "blocks")
public class GuardedLoot extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    GuardedLoot() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    @ToString
    public static class Config extends GameConfig {
        public NumberIntermediary range = NumberIntermediary.of(4);
        public boolean allowLockPicking = true;
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
