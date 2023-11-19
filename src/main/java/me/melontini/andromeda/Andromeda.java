package me.melontini.andromeda;

import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.CommonValues;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Objects;

public class Andromeda {

    private static Andromeda INSTANCE;

    public static void init() {
        INSTANCE = new Andromeda();
        INSTANCE.onInitialize();
        FabricLoader.getInstance().getObjectShare().put("andromeda:main", INSTANCE);
    }

    private void onInitialize() {
        AndromedaReporter.initCrashHandler();
        Common.bootstrap();
    }

    @Override
    public String toString() {
        return "Andromeda{version=" + CommonValues.version() + "}";
    }

    public static Andromeda get() {
        return Objects.requireNonNull(INSTANCE, "Andromeda not initialized");
    }
}
