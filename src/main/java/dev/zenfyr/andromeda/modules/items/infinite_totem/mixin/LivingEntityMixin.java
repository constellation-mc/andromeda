package dev.zenfyr.andromeda.modules.items.infinite_totem.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemMain;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

  @Shadow
  public abstract ItemStack getItemInHand(InteractionHand hand);

  public LivingEntityMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @WrapWithCondition(
      method = "checkTotemDeathProtection",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
  private boolean andromeda$infiniteFallback(ItemStack instance, int i) {
    return !instance.is(InfiniteTotemMain.INFINITE_TOTEM.orThrow());
  }
}
