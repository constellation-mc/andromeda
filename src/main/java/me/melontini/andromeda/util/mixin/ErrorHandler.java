package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.util.CrashHandler;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.Debug;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

@CustomLog
public class ErrorHandler implements IMixinErrorHandler {

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return handleMixinError("prepare", th, mixin, action);
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return handleMixinError("apply", th, mixin, action);
    }

    private static ErrorAction handleMixinError(String phase, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (Debug.hasKey(Debug.Keys.SKIP_MIXIN_ERROR_HANDLER)) return action;

        if (action == ErrorAction.ERROR) {
            if (mixin.getClassName().startsWith("me.melontini.andromeda"))
                CrashHandler.handleCrash(true, th, "Failed to " + phase + " " + mixin.getClassName(), FabricLoader.getInstance().getEnvironmentType());

            ModuleManager.get().moduleFromConfig(mixin.getConfig().getName()).ifPresent(module -> {
                module.config().enabled = false;
                module.save();
                AndromedaLog.info("Disabling module '%s'!".formatted(module.meta().id()));
            });
            return action;
        }
        return action;
    }
}
