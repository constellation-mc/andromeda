package dev.zenfyr.andromeda.modules.items.infinite_totem.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.items.infinite_totem.Main;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.UsedCustomTotemPayload;
import dev.zenfyr.pulsar.api.util.PlayerUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

  @Shadow
  public abstract ItemStack getItemInHand(InteractionHand hand);

  public LivingEntityMixin(EntityType<?> type, Level world) {
    super(type, world);
  }

  @WrapWithCondition(
      method = "checkTotemDeathProtection",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
  private boolean andromeda$infiniteFallback(ItemStack instance, int i) {
    return !instance.is(Main.INFINITE_TOTEM.orThrow());
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V",
              shift = At.Shift.BEFORE),
      method = "checkTotemDeathProtection",
      cancellable = true)
  private void andromeda$useInfiniteTotem(
      DamageSource source,
      CallbackInfoReturnable<Boolean> cir,
      @Local(ordinal = 0) ItemStack itemStack) {
    if (itemStack.is(Main.INFINITE_TOTEM.orThrow())) {
      if (!level.isClientSide()) {
        var payload = new UsedCustomTotemPayload(
            this.getUUID(),
            new ItemStack(Main.INFINITE_TOTEM.orThrow()),
            Main.KNOCKOFF_TOTEM_PARTICLE.orThrow());
        for (Player player : PlayerUtil.findPlayersInRange(level, blockPosition(), 120)) {
          ServerPlayNetworking.send((ServerPlayer) player, payload);
        }
      }
      cir.setReturnValue(true);
    }
  }
}
