package dev.zenfyr.andromeda.common.config;

import com.google.gson.GsonBuilder;
import dev.zenfyr.pulsar.api.event.Bus;

public interface GsonBuilderEvent {

  Bus<GsonBuilderEvent> BUS = Bus.create(GsonBuilderEvent.class, events -> builder -> {
    for (GsonBuilderEvent event : events) {
      event.acceptGsonBuilder(builder);
    }
  });

  void acceptGsonBuilder(GsonBuilder builder);
}
