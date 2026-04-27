package dev.zenfyr.andromeda.modules.entities.boats.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SoundPayloadHolder {

  private static boolean done = false;

  public static void init() {
    if (done) return;
    PayloadTypeRegistry.playS2C()
        .register(RecordPlaybackS2CPayload.ID, RecordPlaybackS2CPayload.CODEC);
    done = true;
  }
}
