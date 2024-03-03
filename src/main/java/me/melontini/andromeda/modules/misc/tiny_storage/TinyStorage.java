package me.melontini.andromeda.modules.misc.tiny_storage;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.common.util.TranslationKeyProvider;

import java.util.List;
import java.util.Optional;

@Unscoped
@ModuleInfo(name = "tiny_storage", category = "misc", environment = Environment.SERVER)
public class TinyStorage extends Module<TinyStorage.Config> {

    public static final ThreadLocal<Boolean> LOADING = ThreadLocal.withInitial(() -> false);

    TinyStorage() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

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
