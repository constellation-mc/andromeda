package me.melontini.andromeda.common.client;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.client.sound.PersistentMovingSoundInstance;
import me.melontini.andromeda.modules.entities.boats.BoatEntities;
import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.util.AndromedaPackets;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class ClientSideNetworking {

    public static Map<UUID, PersistentMovingSoundInstance> soundInstanceMap = new ConcurrentHashMap<>();

    public static void register() {
        if (MinecartEntities.JUKEBOX_MINECART_ENTITY.isPresent() || BoatEntities.BOAT_WITH_JUKEBOX.isPresent()) {
            ClientPlayNetworking.registerGlobalReceiver(AndromedaPackets.JUKEBOX_START_PLAYING, (client, handler, buf, responseSender) -> {
                UUID id = buf.readUuid();
                ItemStack stack = buf.readItemStack();
                client.execute(() -> {
                    Entity entity = MakeSure.notNull(client.world, "client.world").getEntityLookup().get(id);
                    if (stack.getItem() instanceof MusicDiscItem disc) {
                        var discName = disc.getDescription();
                        soundInstanceMap.computeIfAbsent(id, k -> {
                            var instance = new PersistentMovingSoundInstance(disc.getSound(), SoundCategory.RECORDS, id, client.world);
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
            ClientPlayNetworking.registerGlobalReceiver(AndromedaPackets.JUKEBOX_STOP_PLAYING, (client, handler, buf, responseSender) -> {
                UUID id = buf.readUuid();
                client.execute(() -> {
                    SoundInstance instance = soundInstanceMap.remove(id);
                    if (client.getSoundManager().isPlaying(instance)) client.getSoundManager().stop(instance);
                });
            });
        }

        ClientPlayNetworking.registerGlobalReceiver(AndromedaPackets.ADD_ONE_PARTICLE, (client, handler, packetByteBuf, responseSender) -> {
            DefaultParticleType particle = (DefaultParticleType) MakeSure.notNull(Registry.PARTICLE_TYPE.get(packetByteBuf.readIdentifier()));
            double x = packetByteBuf.readDouble(), y = packetByteBuf.readDouble(), z = packetByteBuf.readDouble();
            double velocityX = packetByteBuf.readDouble(), velocityY = packetByteBuf.readDouble(), velocityZ = packetByteBuf.readDouble();
            client.execute(() -> client.worldRenderer.addParticle(particle, particle.shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ));
        });

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(MODID, "notify_client_about_stuff_please"), (client, handler, packetByteBuf, responseSender) -> {
            int uuid = packetByteBuf.readVarInt();
            ItemStack stack = packetByteBuf.readItemStack();
            client.execute(() -> {
                ItemEntity entity = (ItemEntity) MakeSure.notNull(client.world, "client.world").getEntityLookup().get(uuid);
                if (entity != null) entity.getDataTracker().set(ItemEntity.STACK, stack);
            });
        });

        ClientLoginNetworking.registerGlobalReceiver(AndromedaPackets.VERIFY_MODULES, (client, handler, buf, listenerAdder) -> {
            String[] modules = ModuleManager.get().loaded().stream().filter(m->m.meta().environment() == Environment.BOTH)
                    .map(m -> m.meta().id()).toArray(String[]::new);
            var pbf = PacketByteBufs.create();
            pbf.writeVarInt(modules.length);
            for (String module : modules) {
                pbf.writeString(module);
            }
            return CompletableFuture.completedFuture(pbf);
        });
    }
}
