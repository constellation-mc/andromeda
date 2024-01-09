package me.melontini.andromeda.base.workarounds.pre_launch;

import lombok.CustomLog;
import me.melontini.andromeda.util.CommonValues;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Some mods like to load MC classes during {@code preLaunch}, which causes Andromeda to crash.
 * We need to move our {@code preLaunch} to the top. This may be Quilt specific, but just to be safe, the fix applies to Fabric as well.
 * <p>If this doesn't work, I'll have to resort to {@link org.spongepowered.asm.mixin.transformer.ext.IExtension}. The ultimate entrypoint :)</p>
 */
@CustomLog
public class PreLaunchWorkaround {

    public static void pushPreLaunch() {
        try {
            switch (CommonValues.platform()) {
                case FABRIC, CONNECTOR -> {
                    LOGGER.info("Trying Fabric-style entrypoint push!");
                    new FabricPreLaunch().pushPreLaunch();
                }
                case QUILT -> {
                    LOGGER.info("Trying Quilt-style entrypoint push!");
                    new QuiltPreLaunch().pushPreLaunch();
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("Entrypoint push failed! This can invalidate the loading state and cause Andromeda to crash!", t);
        }
    }

    public static Set<String> findCandidates() {
        Set<String> set = new LinkedHashSet<>();

        try {
            Class<?> PreLaunchEntrypoint = Class.forName("org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint");
            set.addAll(FabricLoader.getInstance().getEntrypointContainers("pre_launch", PreLaunchEntrypoint).stream().map(EntrypointContainer::getProvider).map(ModContainer::getMetadata).map(ModMetadata::getId).toList());
        } catch (Exception e) {}

        set.addAll(FabricLoader.getInstance().getEntrypointContainers("preLaunch", PreLaunchEntrypoint.class).stream().map(EntrypointContainer::getProvider).map(ModContainer::getMetadata).map(ModMetadata::getId).toList());
        return set;
    }
}
