package me.melontini.andromeda.modules.items.infinite_totem.client;

import me.melontini.andromeda.modules.items.infinite_totem.Main;
import me.melontini.andromeda.modules.items.infinite_totem.packets.NotifyClientPayload;
import me.melontini.andromeda.modules.items.infinite_totem.packets.UsedCustomTotemPayload;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.sound.SoundEvents;

public class Client {

  public static void init() {
    ClientPlayNetworking.registerGlobalReceiver(
        UsedCustomTotemPayload.ID, (payload, context) -> context.client().execute(() -> {
          var world = MakeSure.notNull(context.client().world, "client.world");
          Entity entity = world.getEntityLookup().get(payload.uuid());

          context
              .client()
              .particleManager
              .addEmitter(
                  MakeSure.notNull(entity, "(Andromeda) Client received invalid entity ID"),
                  payload.particle(),
                  30);
          world.playSound(
              entity.getX(),
              entity.getY(),
              entity.getZ(),
              SoundEvents.ITEM_TOTEM_USE,
              entity.getSoundCategory(),
              1.0F,
              1.0F,
              false);
          if (entity == context.player())
            context.client().gameRenderer.showFloatingItem(payload.stack());
        }));

    ClientPlayNetworking.registerGlobalReceiver(
        NotifyClientPayload.ID, (payload, context) -> context.client().execute(() -> {
          ItemEntity entity = (ItemEntity) MakeSure.notNull(context.client().world, "client.world")
              .getEntityLookup()
              .get(payload.entity());
          if (entity != null) entity.getDataTracker().set(ItemEntity.STACK, payload.stack());
        }));

    Main.KNOCKOFF_TOTEM_PARTICLE.ifPresent(
        t -> ParticleFactoryRegistry.getInstance().register(t, KnockoffTotemParticle.Factory::new));
  }
}
