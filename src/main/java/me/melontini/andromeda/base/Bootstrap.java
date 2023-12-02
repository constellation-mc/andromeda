package me.melontini.andromeda.base;

import com.google.common.reflect.ClassPath;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixins;

@SuppressWarnings("UnstableApiUsage")
public class Bootstrap {

    public static ModuleManager INSTANCE;
    private static final ClassPath CLASS_PATH = Utilities.supplyUnchecked(() -> ClassPath.from(MixinProcessor.class.getClassLoader()));

    @Environment(EnvType.CLIENT)
    public static void onClient() {
        for (Module<?> module : ModuleManager.get().loaded()) { module.onClient(); }
        AndromedaClient.init();
    }

    @Environment(EnvType.SERVER)
    public static void onServer() {
        for (Module<?> module : ModuleManager.get().loaded()) { module.onServer(); }
    }

    public static void onMain() {
        if (Mixins.getUnvisitedCount() > 0) {
            for (org.spongepowered.asm.mixin.transformer.Config config : Mixins.getConfigs()) {
                if (!config.isVisited() && config.getName().startsWith("andromeda$$"))
                    throw new IllegalStateException("Mixin failed to consume Andromeda's late configs!");
            }
        }

        for (Module<?> module : ModuleManager.get().loaded()) { module.onMain(); }
        Andromeda.init();
    }

    public static void onPreLaunch() {
        Config.get();

        ModuleManager m = (INSTANCE = new ModuleManager());

        m.prepare();
        m.print();

        MixinProcessor.addMixins(m);
        for (Module<?> module : m.loaded()) { module.onPreLaunch(); }
    }

    public static ClassPath getKnotClassPath() {
        return CLASS_PATH;
    }
}
