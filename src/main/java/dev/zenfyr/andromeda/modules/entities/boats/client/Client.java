package dev.zenfyr.andromeda.modules.entities.boats.client;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.boats.BoatTypes;
import dev.zenfyr.andromeda.modules.entities.boats.Boats;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public class Client {

  private static <T extends AbstractBoat> void renderer(
      BoatTypes.BoatType type, BoatTypes.BoatVariant variant) {
    var location = BoatTypes.location(type, variant);
    EntityType<T> entityType = (EntityType<T>) BuiltInRegistries.ENTITY_TYPE.getValue(location);

    var modelLocation =
        new ModelLayerLocation(Identifier.tryBuild("minecraft", "boat/" + type.material()), "main");

    EntityRenderers.register(
        entityType,
        context -> new BoatWithBlockRenderer(
            context, variant.state(), type.model().offset(), modelLocation));
  }

  public static void init() {
    var config = Andromeda.MAIN.get(Boats.MAIN_CONFIG);

    for (BoatTypes.BoatType type : BoatTypes.getBoatTypes()) {
      if (config.isTNTBoatOn) renderer(type, BoatTypes.TNT);

      if (config.isHopperBoatOn) renderer(type, BoatTypes.HOPPER);

      if (config.isFurnaceBoatOn) renderer(type, BoatTypes.FURNACE);

      if (config.isJukeboxBoatOn) renderer(type, BoatTypes.JUKEBOX);
    }

    if (config.isJukeboxBoatOn) ClientSoundHolder.init();
  }
}
