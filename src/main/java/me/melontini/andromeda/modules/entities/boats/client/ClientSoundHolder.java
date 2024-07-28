package me.melontini.andromeda.modules.entities.boats.client;

import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.entities.boats.packets.StartPayload;
import me.melontini.andromeda.modules.entities.boats.packets.StopPayload;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ClientSoundHolder {

    public static final Identifier JUKEBOX_START_PLAYING = Andromeda.id("jukebox_start_playing");
    public static final Identifier JUKEBOX_STOP_PLAYING = Andromeda.id("jukebox_stop_playing");

    private static boolean done = false;
    private static final Map<UUID, PersistentMovingSoundInstance> soundInstanceMap = new HashMap<>();

    public static void init() {
        if (done) return;

        ClientPlayNetworking.registerGlobalReceiver(StartPayload.ID, (payload, context) -> context.client().execute(() -> {
            var client = context.client();

            Entity entity = MakeSure.notNull(client.world, "client.world").getEntityLookup().get(payload.entity());
            if (payload.record().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
                var disc = payload.record().get(DataComponentTypes.JUKEBOX_PLAYABLE);
                var songOptional = disc.song().getValue(context.client().getNetworkHandler().getRegistryManager().get(RegistryKeys.JUKEBOX_SONG));

                if (songOptional.isEmpty()) {
                    return;
                }
                var song = songOptional.get();

                soundInstanceMap.computeIfAbsent(payload.entity(), k -> {
                    var instance = new PersistentMovingSoundInstance(song.soundEvent().value(), SoundCategory.RECORDS, k, client.world, Random.create());
                    client.getSoundManager().play(instance);
                    return instance;
                });
                if (client.player != null && entity != null && entity.distanceTo(client.player) < 76) {
                    client.inGameHud.setRecordPlayingOverlay(song.description());
                }
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(StopPayload.ID, (payload, context) -> context.client().execute(() -> {
            SoundInstance instance = soundInstanceMap.remove(payload.entity());
            if (context.client().getSoundManager().isPlaying(instance)) context.client().getSoundManager().stop(instance);
        }));

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
