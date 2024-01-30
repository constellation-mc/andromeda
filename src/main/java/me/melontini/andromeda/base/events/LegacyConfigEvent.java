package me.melontini.andromeda.base.events;

import com.google.gson.JsonObject;

public interface LegacyConfigEvent {

    Bus<LegacyConfigEvent> BUS = new Bus<>(events -> config -> events.forEach(event -> event.acceptLegacy(config)));

    void acceptLegacy(JsonObject config);
}
