package me.melontini.andromeda.base.workarounds.pre_launch;

import lombok.CustomLog;
import lombok.SneakyThrows;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericField;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericMethod;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.entrypoint.EntrypointStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@CustomLog
public class FabricPreLaunch {

    FabricPreLaunch() { }

    @SneakyThrows
    void pushPreLaunch() {
        GenericField<FabricLoaderImpl, EntrypointStorage> esField = GenericField.of(FabricLoaderImpl.class, "entrypointStorage");
        GenericField<EntrypointStorage, Map<String, List<?>>> emField = GenericField.of(EntrypointStorage.class, "entryMap");

        esField.accessible(true);
        emField.accessible(true);

        EntrypointStorage storage = esField.get(FabricLoaderImpl.INSTANCE);
        var entryMap = emField.get(storage);

        var EntrypointStorage$Entry = Class.forName("net.fabricmc.loader.impl.entrypoint.EntrypointStorage$Entry");
        GenericMethod<?, ModContainerImpl> getModContainer = GenericMethod.of(EntrypointStorage$Entry, "getModContainer");
        entryMap.get("preLaunch").sort(Comparator.comparingInt(value -> {
            try {
                ModContainerImpl container = getModContainer.invoke(Utilities.cast(value));
                if (container.getMetadata().getId().equals("andromeda")) return 0;
            } catch (Throwable t) {
                return 1;
            }
            return 1;
        }));

        LOGGER.debug(entryMap.get("preLaunch"));
        LOGGER.info("Pushed entrypoint successfully!");
    }
}
