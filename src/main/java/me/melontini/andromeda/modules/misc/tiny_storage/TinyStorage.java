package me.melontini.andromeda.modules.misc.tiny_storage;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.util.TranslationKeyProvider;

import java.util.Optional;

@ModuleInfo(name = "tiny_storage", category = "misc", environment = Environment.SERVER)
public final class TinyStorage extends Module {

    public static final ThreadLocal<Boolean> LOADING = ThreadLocal.withInitial(() -> false);
    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    TinyStorage() {
        this.defineConfig(ConfigState.MAIN, CONFIG);
        InitEvent.main(this).listen(() -> Main::init);
    }

    @ToString
    public static class Config extends BaseConfig {
        public TransferMode transferMode = TransferMode.FOLLOW_GAMERULE;
    }

    public enum TransferMode implements TranslationKeyProvider {
        FOLLOW_GAMERULE,
        ALWAYS_TRANSFER;

        @Override
        public Optional<String> getTranslationKey() {
            return Optional.of("config.andromeda.misc.tiny_storage.option.TransferMode." + name());
        }
    }
}
