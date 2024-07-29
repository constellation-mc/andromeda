package me.melontini.andromeda.base.events;

import com.google.gson.GsonBuilder;

public interface ConfigGsonEvent {

  Bus<ConfigGsonEvent> BUS =
      new Bus<>(events -> (builder) -> events.forEach(event -> event.accept(builder)));

  void accept(GsonBuilder builder);
}
