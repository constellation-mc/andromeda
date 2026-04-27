package dev.zenfyr.andromeda.modules.entities.boats.client;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.boats.BoatTypes;
import dev.zenfyr.andromeda.modules.entities.boats.Boats;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractBoat;

public class Client {

  private static <T extends AbstractBoat> void renderer(
      BoatTypes.BoatType type, BoatTypes.BoatVariant variant) {
    var location = BoatTypes.location(type, variant);
    EntityType<T> entityType = (EntityType<T>) BuiltInRegistries.ENTITY_TYPE.getValue(location);

    var modelLocation =
        new ModelLayerLocation(id(variant.name() + "_boat/" + type.material()), "main");

    EntityModelLayerRegistry.TexturedModelDataProvider provider;

    if ("boat".equals(type.model().name())) {
      provider = BoatModel::createBoatModel;
    } else if ("raft".equals(type.model().name())) {
      provider = RaftModel::createRaftModel;
    } else {
      throw new RuntimeException("No model provider for variant! '%s'".formatted(variant.name()));
    }

    EntityModelLayerRegistry.registerModelLayer(modelLocation, provider);
    EntityRenderers.register(entityType, context -> new BoatRenderer(context, modelLocation));
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
