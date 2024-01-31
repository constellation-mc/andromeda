package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.modules.entities.boats.entities.FurnaceBoatEntity;
import me.melontini.andromeda.modules.entities.boats.entities.HopperBoatEntity;
import me.melontini.andromeda.modules.entities.boats.entities.JukeboxBoatEntity;
import me.melontini.andromeda.modules.entities.boats.entities.TNTBoatEntity;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static me.melontini.andromeda.common.registries.Common.id;

public class BoatEntities {

    public static final Keeper<EntityType<TNTBoatEntity>> BOAT_WITH_TNT = Keeper.create();
    public static final Keeper<EntityType<FurnaceBoatEntity>> BOAT_WITH_FURNACE = Keeper.create();
    public static final Keeper<EntityType<JukeboxBoatEntity>> BOAT_WITH_JUKEBOX = Keeper.create();
    public static final Keeper<EntityType<HopperBoatEntity>> BOAT_WITH_HOPPER = Keeper.create();

    private static @Nullable <T extends Entity> EntityType<T> boatType(boolean register, Identifier id, EntityType.EntityFactory<T> factory) {
        return RegistryUtil.createEntityType(register, id,
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory)
                        .dimensions(new EntityDimensions(1.375F, 0.5625F, true)));
    }

    public static void init(Boats.Config config) {
        BOAT_WITH_TNT.init(boatType(config.isTNTBoatOn, id("tnt_boat"), TNTBoatEntity::new));
        BOAT_WITH_FURNACE.init(boatType(config.isFurnaceBoatOn, id("furnace_boat"), FurnaceBoatEntity::new));
        BOAT_WITH_JUKEBOX.init(boatType(config.isJukeboxBoatOn, id("jukebox_boat"), JukeboxBoatEntity::new));
        BOAT_WITH_HOPPER.init(boatType(config.isHopperBoatOn, id("hopper_boat"), HopperBoatEntity::new));

        BOAT_WITH_TNT.ifPresent(e -> ServerPlayNetworking.registerGlobalReceiver(TNTBoatEntity.EXPLODE_BOAT_ON_SERVER,
                (server, player, handler, buf, responseSender) -> {
                    UUID id = buf.readUuid();
                    server.execute(() -> {
                        Entity entity = player.world.getEntityLookup().get(id);
                        if (entity instanceof TNTBoatEntity boat && boat.isAlive()) boat.explode();
                    });
                }));
    }
}
