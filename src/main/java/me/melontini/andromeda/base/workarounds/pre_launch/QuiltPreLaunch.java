package me.melontini.andromeda.base.workarounds.pre_launch;

import lombok.CustomLog;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericField;
import me.melontini.dark_matter.api.base.util.Utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CustomLog
public class QuiltPreLaunch {

    Object loader;
    Field esField;
    GenericField<?, Map<String, List<Object>>> emField;
    Class<?> EntrypointStorage$Entry;
    Class<?> PreLaunchEntrypoint;

    QuiltPreLaunch() {
        try {
            Class<?> QuiltLoaderImpl = Class.forName("org.quiltmc.loader.impl.QuiltLoaderImpl");
            Field INSTANCE = QuiltLoaderImpl.getField("INSTANCE");
            loader = INSTANCE.get(null);

            esField = QuiltLoaderImpl.getDeclaredField("entrypointStorage");
            esField.setAccessible(true);

            Class<?> EntrypointStorage = Class.forName("org.quiltmc.loader.impl.entrypoint.EntrypointStorage");
            EntrypointStorage$Entry = Class.forName("org.quiltmc.loader.impl.entrypoint.EntrypointStorage$Entry");
            PreLaunchEntrypoint = Class.forName("org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint");

            emField = GenericField.of(EntrypointStorage, "entryMap");
            emField.accessible(true);
        } catch (Throwable t) {
            LOGGER.error("Failed to prepare Quilt-style push!", t);
        }
    }

    boolean pushPreLaunch() {
        if (loader == null || esField == null || emField == null) {
            LOGGER.error("Quilt-style entrypoint push failed! Internals changed! :(");
            return false;
        }

        try {
            var realEs = esField.get(loader);

            var entryMap = emField.get(Utilities.cast(realEs));

            var itr = entryMap.get("preLaunch").iterator();
            while (itr.hasNext()) {
                var value = itr.next();

                if (value.toString().startsWith("andromeda->")) {
                    itr.remove(); var invoker = invoker(value);

                    //We have to use proxies, since Quilt adds a new param.
                    entryMap.computeIfAbsent("pre_launch", string -> new ArrayList<>()).add(0,
                            Proxy.newProxyInstance(loader.getClass().getClassLoader(), new Class[]{ EntrypointStorage$Entry }, (proxy, method, args) -> {
                                if ("getOrCreate".equals(method.getName())) {
                                    return invoker;
                                }
                                method.setAccessible(true);
                                return method.invoke(value, args);
                            }));
                    break;
                }
            }

            LOGGER.debug(entryMap.get("preLaunch"));
            LOGGER.info("Pushed entrypoint successfully!");
        } catch (Exception e) {
            LOGGER.error("Quilt-style entrypoint push failed!", e);
            return false;
        }
        return true;
    }

    private Object invoker(Object value) {
        return Proxy.newProxyInstance(loader.getClass().getClassLoader(), new Class[]{ PreLaunchEntrypoint }, (proxy, method, args) -> {
            if ("onPreLaunch".equals(method.getName())) {
                Bootstrap.onPreLaunch();
                return null;
            }
            method.setAccessible(true);
            return method.invoke(value, args);
        });
    }
}
