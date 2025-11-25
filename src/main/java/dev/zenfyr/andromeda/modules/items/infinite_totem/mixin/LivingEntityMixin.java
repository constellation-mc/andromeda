package dev.zenfyr.andromeda.modules.items.infinite_totem.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotem;
import dev.zenfyr.andromeda.modules.items.infinite_totem.Main;
import me.melontini.dark_matter.api.minecraft.util.PlayerUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
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

  @ModifyExpressionValue(
      method = "checkTotemDeathProtection",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
  private boolean andromeda$infiniteFallback(
      boolean original, DamageSource source, @Local(index = 3) ItemStack itemStack) {
    return original
        || (level.am$get(InfiniteTotem.CONFIG).available
            && itemStack.is(Main.INFINITE_TOTEM.orThrow()));
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
        FriendlyByteBuf buf = PacketByteBufs.create()
            .writeUUID(this.getUUID())
            .writeItem(new ItemStack(Main.INFINITE_TOTEM.orThrow()));
        buf.writeId(BuiltInRegistries.PARTICLE_TYPE, Main.KNOCKOFF_TOTEM_PARTICLE.orThrow());

        for (Player player : PlayerUtil.findPlayersInRange(level, blockPosition(), 120)) {
          ServerPlayNetworking.send((ServerPlayer) player, Main.USED_CUSTOM_TOTEM, buf);
        }
      }
      cir.setReturnValue(true);
    }
  }
}
