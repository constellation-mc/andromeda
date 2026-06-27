package dev.zenfyr.andromeda.modules.items.infinite_totem.client;

import dev.zenfyr.andromeda.modules.items.infinite_totem.Main;
import dev.zenfyr.pulsar.api.util.MakeSure;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class Client {

  public static void init() {
    ClientPlayNetworking.registerGlobalReceiver(
        Main.USED_CUSTOM_TOTEM, (client, handler, buf, responseSender) -> {
          UUID id = buf.readUUID();
          ItemStack stack = buf.readItem();
          SimpleParticleType particle =
              (SimpleParticleType) buf.readById(BuiltInRegistries.PARTICLE_TYPE);
          client.execute(() -> {
            Entity entity =
                MakeSure.notNull(client.level, "client.world").getEntities().get(id);
            client.particleEngine.createTrackingEmitter(
                MakeSure.notNull(entity, "(Andromeda) Client received invalid entity ID"),
                particle,
                30);
            client.level.playLocalSound(
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                SoundEvents.TOTEM_USE,
                entity.getSoundSource(),
                1.0F,
                1.0F,
                false);
            if (entity == client.player) client.gameRenderer.displayItemActivation(stack);
          });
        });

    ClientPlayNetworking.registerGlobalReceiver(
        Main.NOTIFY_CLIENT, (client, handler, packetByteBuf, responseSender) -> {
          int uuid = packetByteBuf.readVarInt();
          ItemStack stack = packetByteBuf.readItem();
          client.execute(() -> {
            ItemEntity entity = (ItemEntity)
                MakeSure.notNull(client.level, "client.world").getEntities().get(uuid);
            if (entity != null) entity.getEntityData().set(ItemEntity.DATA_ITEM, stack);
          });
        });

    if (Main.KNOCKOFF_TOTEM_PARTICLE.isPresent()) {
      ParticleFactoryRegistry.getInstance()
          .register(Main.KNOCKOFF_TOTEM_PARTICLE.orThrow(), KnockoffTotemParticle.Factory::new);
    }
  }
}
