package me.melontini.andromeda.base.events;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.client.config.FeatureBlockade;

public interface BlockadesEvent {

    Bus<BlockadesEvent> BUS = new Bus<>(events -> (manager, blockade) -> events.forEach(event -> event.explain(manager, blockade)));

    void explain(ModuleManager manager, FeatureBlockade blockade);
}
