package me.melontini.andromeda.networks;

import me.melontini.andromeda.Andromeda;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.world.explosion.Explosion;

import java.util.Objects;
import java.util.UUID;

public class ServerSideNetworking {

    public static void register() {
        if (Andromeda.CONFIG.newBoats.isTNTBoatOn)
            ServerPlayNetworking.registerGlobalReceiver(AndromedaPackets.EXPLODE_BOAT_ON_SERVER, (server, player, handler, buf, responseSender) -> {
                UUID id = buf.readUuid();
                server.execute(() -> {
                    Entity entity = player.world.getEntityLookup().get(id);
                    Objects.requireNonNull(entity, String.format("(Andromeda) Server Received Invalid TNT Boat UUID: %s", id)).discard();
                    player.world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), 4.0F, Explosion.DestructionType.DESTROY);
                });
            });
    }
}
