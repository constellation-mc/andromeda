package dev.zenfyr.andromeda.modules.entities.boats.client;

import dev.zenfyr.andromeda.modules.entities.boats.packets.RecordPlaybackS2CPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.JukeboxSong;

public class ClientSoundHolder {

  private static volatile boolean done = false;
  private static final Map<UUID, PersistentMovingSoundInstance> soundInstanceMap = new HashMap<>();

  public static void init() {
    if (done) return;

    ClientPlayNetworking.registerGlobalReceiver(RecordPlaybackS2CPayload.ID, (payload, context) -> {
      var client = context.client();
      var entity = client.level.getEntity(payload.entity());

      if (payload.record().isEmpty() || !payload.record().has(DataComponents.JUKEBOX_PLAYABLE)) {
        var instance = soundInstanceMap.remove(payload.entity());
        if (instance != null && client.getSoundManager().isActive(instance)) {
          client.getSoundManager().stop(instance);
        }
        return;
      }

      var songOptional = JukeboxSong.fromStack(payload.record());
      if (songOptional.isEmpty()) return;
      var song = songOptional.get();
      soundInstanceMap.computeIfAbsent(payload.entity(), uuid -> {
        var instance = new PersistentMovingSoundInstance(
            song.value().soundEvent().value(),
            SoundSource.RECORDS,
            uuid,
            client.level,
            RandomSource.create());
        client.getSoundManager().play(instance);
        return instance;
      });

      if (client.player != null && entity != null && entity.distanceTo(client.player) < 76) {
        client.gui.hud.setNowPlaying(song.value().description());
      }
    });

    done = true;
  }

  public static class PersistentMovingSoundInstance extends AbstractTickableSoundInstance {

    private final ClientLevel level;
    private final UUID entityId;

    public PersistentMovingSoundInstance(
        SoundEvent soundEvent,
        SoundSource soundCategory,
        UUID entityId,
        ClientLevel level,
        RandomSource random) {
      super(soundEvent, soundCategory, random);
      this.volume = 3;
      this.pitch = 1;
      this.level = level;
      this.entityId = entityId;
    }

    @Override
    public void tick() {
      Entity entity = level.getEntities().get(entityId);
      if (entity != null) {
        this.volume = 3;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
      } else {
        // this sucks
        this.volume = 0;
      }
    }
  }
}
