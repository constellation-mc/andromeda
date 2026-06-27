package dev.zenfyr.andromeda.modules.entities.boats;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.entities.boats.entities.FurnaceBoatEntity;
import dev.zenfyr.andromeda.modules.entities.boats.entities.HopperBoatEntity;
import dev.zenfyr.andromeda.modules.entities.boats.entities.JukeboxBoatEntity;
import dev.zenfyr.andromeda.modules.entities.boats.entities.TNTBoatEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;

public class BoatEntities {

  public static final Keeper<EntityType<TNTBoatEntity>> BOAT_WITH_TNT = Keeper.create();
  public static final Keeper<EntityType<FurnaceBoatEntity>> BOAT_WITH_FURNACE = Keeper.create();
  public static final Keeper<EntityType<JukeboxBoatEntity>> BOAT_WITH_JUKEBOX = Keeper.create();
  public static final Keeper<EntityType<HopperBoatEntity>> BOAT_WITH_HOPPER = Keeper.create();

  private static @Nullable <T extends Entity> EntityType<T> boatType(
      boolean register, ResourceLocation id, EntityType.EntityFactory<T> factory) {
    if (register) {
      return Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id,
          FabricEntityTypeBuilder.create(MobCategory.MISC, factory)
              .dimensions(new EntityDimensions(1.375F, 0.5625F, true))
              .build());
    } else {
      return null;
    }
  }

  public static void init() {
    var config = Andromeda.MAIN.get(Boats.MAIN_CONFIG);
    BOAT_WITH_TNT.init(boatType(config.isTNTBoatOn, id("tnt_boat"), TNTBoatEntity::new));
    BOAT_WITH_FURNACE.init(
        boatType(config.isFurnaceBoatOn, id("furnace_boat"), FurnaceBoatEntity::new));
    BOAT_WITH_JUKEBOX.init(
        boatType(config.isJukeboxBoatOn, id("jukebox_boat"), JukeboxBoatEntity::new));
    BOAT_WITH_HOPPER.init(
        boatType(config.isHopperBoatOn, id("hopper_boat"), HopperBoatEntity::new));

    if (BOAT_WITH_TNT.isPresent()) {
      // This sucks
      ServerPlayNetworking.registerGlobalReceiver(
          TNTBoatEntity.EXPLODE_BOAT_ON_SERVER, (server, player, handler, buf, responseSender) -> {
            UUID id = buf.readUUID();
            server.execute(() -> {
              Entity entity = player.serverLevel().getEntity(id);
              if (entity instanceof TNTBoatEntity boat
                  && boat.isAlive()
                  && player == boat.getFirstPassenger()) boat.explode();
            });
          });
    }
  }
}
