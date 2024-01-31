package me.melontini.andromeda.modules.entities.boats.client;

import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class ClientSoundHolder {

    public static final Identifier JUKEBOX_START_PLAYING = new Identifier(MODID, "jukebox_start_playing");
    public static final Identifier JUKEBOX_STOP_PLAYING = new Identifier(MODID, "jukebox_stop_playing");

    private static volatile boolean done = false;
    private static final Map<UUID, PersistentMovingSoundInstance> soundInstanceMap = new HashMap<>();

    public static void init() {
        if (done) return;

        ClientPlayNetworking.registerGlobalReceiver(JUKEBOX_START_PLAYING, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            ItemStack stack = buf.readItemStack();
            client.execute(() -> {
                Entity entity = MakeSure.notNull(client.world, "client.world").getEntityLookup().get(id);
                if (stack.getItem() instanceof MusicDiscItem disc) {
                    var discName = disc.getDescription();
                    soundInstanceMap.computeIfAbsent(id, k -> {
                        var instance = new PersistentMovingSoundInstance(disc.getSound(), SoundCategory.RECORDS, id, client.world, Random.create());
                        client.getSoundManager().play(instance);
                        return instance;
                    });
                    if (discName != null) {
                        if (client.player != null && entity != null && entity.distanceTo(client.player) < 76) {
                            client.inGameHud.setRecordPlayingOverlay(discName);
                        }
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(JUKEBOX_STOP_PLAYING, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            client.execute(() -> {
                SoundInstance instance = soundInstanceMap.remove(id);
                if (client.getSoundManager().isPlaying(instance)) client.getSoundManager().stop(instance);
            });
        });

        done = true;
    }

    public static class PersistentMovingSoundInstance extends MovingSoundInstance {

        private final ClientWorld world;
        private final UUID entityId;

        public PersistentMovingSoundInstance(SoundEvent soundEvent, SoundCategory soundCategory, UUID entityId, ClientWorld world, Random random) {
            super(soundEvent, soundCategory, random);
            this.volume = 3;
            this.pitch = 1;
            this.world = world;
            this.entityId = entityId;
        }

        @Override
        public void tick() {
            Entity entity = world.getEntityLookup().get(entityId);
            if (entity != null) {
                this.volume = 3;
                this.x = entity.getX();
                this.y = entity.getY();
                this.z = entity.getZ();
            } else {
                //this sucks
                this.volume = 0;
            }
        }
    }
}
