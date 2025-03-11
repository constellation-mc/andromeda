package me.melontini.andromeda.common.config;

import com.google.gson.GsonBuilder;
import me.melontini.andromeda.bootstrap.event.bus.Bus;

public interface GsonBuilderEvent {

  Bus<GsonBuilderEvent> BUS = Bus.create(GsonBuilderEvent.class, events -> builder -> {
    for (GsonBuilderEvent event : events) {
      event.acceptGsonBuilder(builder);
    }
  });

  void acceptGsonBuilder(GsonBuilder builder);
}
