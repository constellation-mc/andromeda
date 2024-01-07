package me.melontini.andromeda.base.hacks;

import lombok.CustomLog;
import me.melontini.andromeda.util.CommonValues;

/**
 * Some mods like to load MC classes during {@code preLaunch}, which causes Andromeda to crash.
 * We need to move our {@code preLaunch} to the top. This may be Quilt specific, but just to be safe, the fix applies to Fabric as well.
 * <p>If this doesn't work, I'll have to resort to {@link org.spongepowered.asm.mixin.transformer.ext.IExtension}. The ultimate entrypoint :)</p>
 */
@CustomLog
public class EntrypointHack {

    public static boolean pushPreLaunch() {
        try {
            return switch (CommonValues.platform()) {
                case FABRIC, CONNECTOR -> {
                    LOGGER.info("Trying Fabric-style entrypoint push!");
                     yield new FabricEntrypointHack().pushPreLaunch();
                }
                case QUILT -> {
                    LOGGER.info("Trying Quilt-style entrypoint push!");
                    yield new QuiltEntrypointHack().pushPreLaunch();
                }
                default -> true;
            };
        } catch (Throwable t) {
            return false;
        }
    }
}
