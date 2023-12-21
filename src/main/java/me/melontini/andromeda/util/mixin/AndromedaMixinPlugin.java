package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.base.Debug;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import org.spongepowered.asm.mixin.Mixins;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class AndromedaMixinPlugin extends ExtendablePlugin {

    @Override
    public void onPluginLoad(String mixinPackage) {
        Debug.load();
        try {
            FrameworkPatch.patch();
        } catch (Throwable e) {
            LOGGER.error("Failed to patch the mixin framework!", e);
        }

        Mixins.registerErrorHandlerClass(ErrorHandler.class.getName());
    }
}
