package me.melontini.andromeda.common.data;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.registry.DynamicRegistryManager;

import java.util.Arrays;
import java.util.function.Consumer;

public interface ServerResourceReloadersEvent {

    Event<ServerResourceReloadersEvent> EVENT = EventFactory.createArrayBacked(ServerResourceReloadersEvent.class, events ->
            (c) -> Arrays.stream(events).forEach(e -> e.register(c)));

    void register(Context context);

    record Context(DynamicRegistryManager.Immutable dynamicRegistryManager,
                   CommandManager.RegistrationEnvironment environment, Consumer<IdentifiableResourceReloadListener> registrar) {

        public void register(IdentifiableResourceReloadListener listener) {
            registrar().accept(listener);
        }
    }
}
