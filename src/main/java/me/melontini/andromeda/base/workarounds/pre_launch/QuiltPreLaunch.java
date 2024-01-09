package me.melontini.andromeda.base.workarounds.pre_launch;

import lombok.CustomLog;
import lombok.SneakyThrows;
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

    QuiltPreLaunch() { }

    @SneakyThrows
    void pushPreLaunch() {
        Class<?> QuiltLoaderImpl = Class.forName("org.quiltmc.loader.impl.QuiltLoaderImpl");
        Field INSTANCE = QuiltLoaderImpl.getField("INSTANCE");
        Object loader = INSTANCE.get(null);

        Field esField = QuiltLoaderImpl.getDeclaredField("entrypointStorage");
        esField.setAccessible(true);

        Class<?> EntrypointStorage = Class.forName("org.quiltmc.loader.impl.entrypoint.EntrypointStorage");
        Class<?> EntrypointStorage$Entry = Class.forName("org.quiltmc.loader.impl.entrypoint.EntrypointStorage$Entry");
        Class<?> PreLaunchEntrypoint = Class.forName("org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint");

        GenericField<?, Map<String, List<Object>>> emField = GenericField.of(EntrypointStorage, "entryMap");
        emField.accessible(true);

        var realEs = esField.get(loader);

        var entryMap = emField.get(Utilities.cast(realEs));

        var itr = entryMap.get("preLaunch").iterator();
        while (itr.hasNext()) {
            var value = itr.next();

            if (value.toString().startsWith("andromeda->")) {
                itr.remove(); var invoker = Proxy.newProxyInstance(loader.getClass().getClassLoader(), new Class[]{ PreLaunchEntrypoint }, (proxy, method, args) -> {
                    if ("onPreLaunch".equals(method.getName())) {
                        Bootstrap.shake();
                        return null;
                    }
                    method.setAccessible(true);
                    return method.invoke(value, args);
                });

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
    }
}
