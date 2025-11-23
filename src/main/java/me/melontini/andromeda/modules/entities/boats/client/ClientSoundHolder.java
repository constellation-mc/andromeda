package me.melontini.andromeda.modules.entities.boats.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class ClientSoundHolder {

  public static final ResourceLocation JUKEBOX_START_PLAYING = Andromeda.id("jukebox_start_playing");
  public static final ResourceLocation JUKEBOX_STOP_PLAYING = Andromeda.id("jukebox_stop_playing");

  private static volatile boolean done = false;
  private static final Map<UUID, PersistentMovingSoundInstance> soundInstanceMap = new HashMap<>();

  public static void init() {
    if (done) return;

    ClientPlayNetworking.registerGlobalReceiver(
        JUKEBOX_START_PLAYING, (client, handler, buf, responseSender) -> {
          UUID id = buf.readUUID();
          ItemStack stack = buf.readItem();
          client.execute(() -> {
            Entity entity =
                MakeSure.notNull(client.level, "client.world").getEntities().get(id);
            if (stack.getItem() instanceof RecordItem disc) {
              var discName = disc.getDisplayName();
              soundInstanceMap.computeIfAbsent(id, k -> {
                var instance = new PersistentMovingSoundInstance(
                    disc.getSound(), SoundSource.RECORDS, id, client.level, RandomSource.create());
                client.getSoundManager().play(instance);
                return instance;
              });
              if (discName != null) {
                if (client.player != null
                    && entity != null
                    && entity.distanceTo(client.player) < 76) {
                  client.gui.setNowPlaying(discName);
                }
              }
            }
          });
        });
    ClientPlayNetworking.registerGlobalReceiver(
        JUKEBOX_STOP_PLAYING, (client, handler, buf, responseSender) -> {
          UUID id = buf.readUUID();
          client.execute(() -> {
            SoundInstance instance = soundInstanceMap.remove(id);
            if (client.getSoundManager().isActive(instance))
              client.getSoundManager().stop(instance);
          });
        });

    done = true;
  }

  public static class PersistentMovingSoundInstance extends AbstractTickableSoundInstance {

    private final ClientLevel world;
    private final UUID entityId;

    public PersistentMovingSoundInstance(
            SoundEvent soundEvent,
            SoundSource soundCategory,
            UUID entityId,
            ClientLevel world,
            RandomSource random) {
      super(soundEvent, soundCategory, random);
      this.volume = 3;
      this.pitch = 1;
      this.world = world;
      this.entityId = entityId;
    }

    @Override
    public void tick() {
      Entity entity = world.getEntities().get(entityId);
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
