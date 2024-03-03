package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.util.Debug;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

@CustomLog
public class ErrorHandler implements IMixinErrorHandler {

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return handleMixinError(mixin, action);
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return handleMixinError(mixin, action);
    }

    private static ErrorAction handleMixinError(IMixinInfo mixin, ErrorAction action) {
        if (Debug.Keys.SKIP_MIXIN_ERROR_HANDLER.isPresent()) return action;

        if (action == ErrorAction.ERROR) {
            ModuleManager.get().getMixinProcessor().fromConfig(mixin.getConfig().getName()).ifPresent(module -> {
                module.config().enabled = false;
                module.save();
                LOGGER.info("Disabling module '%s'!".formatted(module.meta().id()));
            });
            return action;
        }
        return action;
    }
}
