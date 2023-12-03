package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class AndromedaMixinPlugin extends ExtendablePlugin {

    @Override
    public void onPluginLoad(String mixinPackage) {
        try {
            FrameworkPatch.patch();
        } catch (Throwable e) {
            LOGGER.error("Failed to patch the mixin framework!", e);
        }

        Bootstrap.onPluginLoad();
    }
}
