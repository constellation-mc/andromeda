package me.melontini.andromeda.modules.entities.boats.packets.sound;

import me.melontini.andromeda.common.util.OneTimeRunnable;
import me.melontini.andromeda.modules.entities.boats.packets.StartPayload;
import me.melontini.andromeda.modules.entities.boats.packets.StopPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SoundHandler {
    public static final OneTimeRunnable INITIALIZER = OneTimeRunnable.of(() -> {
        PayloadTypeRegistry.playS2C().register(StartPayload.ID, StartPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopPayload.ID, StopPayload.CODEC);
    });
}
