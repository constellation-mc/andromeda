package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.common.util.AndromedaPackets;
import me.melontini.andromeda.modules.entities.boats.entities.FurnaceBoatEntity;
import me.melontini.andromeda.modules.entities.boats.entities.HopperBoatEntity;
import me.melontini.andromeda.modules.entities.boats.entities.JukeboxBoatEntity;
import me.melontini.andromeda.modules.entities.boats.entities.TNTBoatEntity;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

import java.util.UUID;

import static me.melontini.andromeda.common.registries.Common.id;

public class BoatEntities {

    private static Boats MODULE;

    public static final Keeper<EntityType<TNTBoatEntity>> BOAT_WITH_TNT = Keeper.of(() ->
            RegistryUtil.createEntityType(() -> MODULE.config().isTNTBoatOn,
                    id("tnt_boat"),
                    FabricEntityTypeBuilder.<TNTBoatEntity>create(SpawnGroup.MISC, TNTBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    public static final Keeper<EntityType<FurnaceBoatEntity>> BOAT_WITH_FURNACE = Keeper.of(() ->
            RegistryUtil.createEntityType(() -> MODULE.config().isFurnaceBoatOn,
                    id("furnace_boat"),
                    FabricEntityTypeBuilder.<FurnaceBoatEntity>create(SpawnGroup.MISC, FurnaceBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    public static final Keeper<EntityType<JukeboxBoatEntity>> BOAT_WITH_JUKEBOX = Keeper.of(() ->
            RegistryUtil.createEntityType(() -> MODULE.config().isJukeboxBoatOn,
                    id("jukebox_boat"),
                    FabricEntityTypeBuilder.<JukeboxBoatEntity>create(SpawnGroup.MISC, JukeboxBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    public static final Keeper<EntityType<HopperBoatEntity>> BOAT_WITH_HOPPER = Keeper.of(() ->
            RegistryUtil.createEntityType(() -> MODULE.config().isHopperBoatOn,
                    id("hopper_boat"),
                    FabricEntityTypeBuilder.<HopperBoatEntity>create(SpawnGroup.MISC, HopperBoatEntity::new)
                            .dimensions(new EntityDimensions(1.375F, 0.5625F, true))));

    public static void init() {
        BOAT_WITH_TNT.ifPresent(e -> ServerPlayNetworking.registerGlobalReceiver(AndromedaPackets.EXPLODE_BOAT_ON_SERVER,
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
