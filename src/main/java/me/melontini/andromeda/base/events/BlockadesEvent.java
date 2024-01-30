package me.melontini.andromeda.base.events;

import me.melontini.andromeda.common.client.config.FeatureBlockade;

public interface BlockadesEvent {

    Bus<BlockadesEvent> BUS = new Bus<>(events -> (blockade) -> events.forEach(event -> event.explain(blockade)));

    void explain(FeatureBlockade blockade);
}
