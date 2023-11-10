package me.melontini.andromeda.networks;

import me.melontini.andromeda.entity.vehicle.boats.TNTBoatEntity;
import me.melontini.andromeda.registries.EntityTypeRegistry;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class ServerSideNetworking {

    public static void register() {
        EntityTypeRegistry.BOAT_WITH_TNT.ifPresent(e -> ServerPlayNetworking.registerGlobalReceiver(AndromedaPackets.EXPLODE_BOAT_ON_SERVER,
                (server, player, handler, buf, responseSender) -> {
                    UUID id = buf.readUuid();
                    server.execute(() -> {
                        Entity entity = player.world.getEntityLookup().get(id);
                        MakeSure.notNull(entity, "(Andromeda) Server Received Invalid TNT Boat UUID: %s".formatted(id));
                        if (entity instanceof TNTBoatEntity boat) boat.explode();
                    });
                }));
    }
}
