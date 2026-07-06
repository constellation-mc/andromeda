package dev.zenfyr.andromeda.modules.items.infinite_totem.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemMain;
import dev.zenfyr.andromeda.modules.items.infinite_totem.client.InfiniteTotemClient;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

  @WrapOperation(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/particle/ParticleEngine;createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V"),
      method = "handleEntityEvent")
  private void andromeda$createInfiniteEmitter(
      ParticleEngine instance,
      Entity entity,
      ParticleOptions particle,
      int lifeTime,
      Operation<Void> original) {
    if (entity instanceof LivingEntity livingEntity) {
      ItemStack stack = InfiniteTotemClient.findTotem(livingEntity);

      if (stack.is(InfiniteTotemMain.INFINITE_TOTEM.orThrow())) {
        original.call(
            instance, entity, InfiniteTotemMain.KNOCKOFF_TOTEM_PARTICLE.orThrow(), lifeTime);
        return;
      }
    }
    original.call(instance, entity, particle, lifeTime);
  }
}
