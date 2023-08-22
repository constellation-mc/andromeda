package me.melontini.andromeda.util;

import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.Collection;
import java.util.function.Consumer;

public class EntrypointRunner {

    public static <T> void runEntrypoint(String entrypoint, Class<T> type, Consumer<? super T> invoker) {
        Collection<EntrypointContainer<T>> entrypoints = FabricLoader.getInstance().getEntrypointContainers(entrypoint, type);
        if (entrypoints.isEmpty()) {
            return;
        }

        for (EntrypointContainer<T> container : entrypoints) {
            try {
                invoker.accept(container.getEntrypoint());
            } catch (Throwable t) {
                String message = String.format("Failed to run ['%s'] due to ['%s'] throwing an exception!", entrypoint, container.getProvider().getMetadata().getId());
                if (container.getProvider().getMetadata().getContact().asMap().containsKey("issues")) {
                    message += " Please report this issue at " + container.getProvider().getMetadata().getContact().asMap().get("issues");
                }
                throw new AndromedaException(false, message, t);
            }
        }
    }

}
