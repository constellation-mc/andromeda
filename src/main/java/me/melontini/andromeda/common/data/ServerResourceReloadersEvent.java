package me.melontini.andromeda.common.data;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.command.CommandManager;

import java.util.Arrays;
import java.util.function.Consumer;

public interface ServerResourceReloadersEvent {

    Event<ServerResourceReloadersEvent> EVENT = EventFactory.createArrayBacked(ServerResourceReloadersEvent.class, events ->
            (c) -> Arrays.stream(events).forEach(e -> e.register(c)));

    void register(Context context);

    record Context(DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures,
                   CommandManager.RegistrationEnvironment environment, Consumer<IdentifiableResourceReloadListener> registrar) {

        public void register(IdentifiableResourceReloadListener listener) {
            registrar().accept(listener);
        }
    }
}
