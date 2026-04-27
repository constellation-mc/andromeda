package dev.zenfyr.andromeda.modules.items.infinite_totem.client;

import dev.zenfyr.andromeda.modules.items.infinite_totem.Main;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.NotifyClientPayload;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.UsedCustomTotemPayload;
import dev.zenfyr.pulsar.util.MakeSure;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public class Client {

  public static void init() {
    ClientPlayNetworking.registerGlobalReceiver(
        UsedCustomTotemPayload.ID, (payload, context) -> context.client().execute(() -> {
          var world = MakeSure.notNull(context.client().level, "client.world");
          Entity entity = world.getEntities().get(payload.uuid());

          context
              .client()
              .particleEngine
              .createTrackingEmitter(
                  MakeSure.notNull(entity, "(Andromeda) Client received invalid entity ID"),
                  payload.particle(),
                  30);
          world.playLocalSound(
              entity.getX(),
              entity.getY(),
              entity.getZ(),
              SoundEvents.TOTEM_USE,
              entity.getSoundSource(),
              1.0F,
              1.0F,
              false);
          if (entity == context.player())
            context.client().gameRenderer.displayItemActivation(payload.stack());
        }));

    ClientPlayNetworking.registerGlobalReceiver(
        NotifyClientPayload.ID, (payload, context) -> context.client().execute(() -> {
          ItemEntity entity = (ItemEntity) MakeSure.notNull(context.client().level, "client.world")
              .getEntities()
              .get(payload.entity());
          if (entity != null) entity.getEntityData().set(ItemEntity.DATA_ITEM, payload.stack());
        }));

    if (Main.KNOCKOFF_TOTEM_PARTICLE.isPresent()) {
      ParticleFactoryRegistry.getInstance()
          .register(Main.KNOCKOFF_TOTEM_PARTICLE.orThrow(), KnockoffTotemParticle.Factory::new);
    }
  }
}
