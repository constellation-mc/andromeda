package dev.zenfyr.andromeda.modules.entities.boats;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.boats.entities.*;
import dev.zenfyr.andromeda.modules.entities.boats.packets.SoundPayloadHolder;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class BoatEntities {

  private interface Factory<T extends AbstractBoat> {
    T create(
        EntityType<T> entityType,
        Level level,
        BoatRideHeightFactory rideHeight,
        Supplier<Item> dropItem);
  }

  private static <T extends AbstractBoat> void boatType(
      BoatTypes.BoatType type, BoatTypes.BoatVariant variant, Factory<T> factory) {
    var location = BoatTypes.location(type, variant);
    Supplier<Item> dropItem = () -> BuiltInRegistries.ITEM.getValue(location);
    BoatRideHeightFactory rideHeight =
        dimensions -> dimensions.height() * type.model().rideHeight();

    var key = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), location);
    Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        key,
        EntityType.Builder.<T>of(
                (entityType, level) -> factory.create(entityType, level, rideHeight, dropItem),
                MobCategory.MISC)
            .sized(1.375F, 0.5625F)
            .build(key));
  }

  public static void init() {
    var config = Andromeda.MAIN.get(Boats.MAIN_CONFIG);

    for (BoatTypes.BoatType type : BoatTypes.getBoatTypes()) {
      if (config.isTNTBoatOn) BoatEntities.boatType(type, BoatTypes.TNT, TNTBoatEntity::new);

      if (config.isFurnaceBoatOn)
        BoatEntities.boatType(type, BoatTypes.FURNACE, FurnaceBoatEntity::new);

      if (config.isJukeboxBoatOn)
        BoatEntities.boatType(type, BoatTypes.JUKEBOX, JukeboxBoatEntity::new);

      if (config.isHopperBoatOn)
        BoatEntities.boatType(type, BoatTypes.HOPPER, HopperBoatEntity::new);
    }

    if (config.isJukeboxBoatOn) {
      SoundPayloadHolder.init();
    }
  }
}
