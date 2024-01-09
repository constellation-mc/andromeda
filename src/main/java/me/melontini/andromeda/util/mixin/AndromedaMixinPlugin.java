package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.base.workarounds.pre_launch.CheckerExtension;
import me.melontini.andromeda.base.workarounds.pre_launch.PreLaunchWorkaround;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import org.spongepowered.asm.mixin.Mixins;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class AndromedaMixinPlugin extends ExtendablePlugin {

    private String mixinPackage;

    @Override
    public void onPluginLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;

        Debug.load();

        if (!Debug.hasKey(Debug.Keys.SKIP_ENTRYPOINT_WORKAROUND))
            PreLaunchWorkaround.pushPreLaunch();

        if (!Debug.hasKey(Debug.Keys.SKIP_LOAD_STATE_VERIFICATION)) {
            try {
                CheckerExtension.add();
            } catch (Throwable e) {
                LOGGER.error(e);
            }
        }

        AndromedaMixins.getClassPath().addUrl(this.getClass().getProtectionDomain().getCodeSource().getLocation());

        Mixins.registerErrorHandlerClass(ErrorHandler.class.getName());
    }

    @Override
    protected void getMixins(List<String> mixins) {
        mixins.addAll(AndromedaMixins.discoverInPackage(this.mixinPackage));
    }
}
