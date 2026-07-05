package dev.zenfyr.andromeda.modules.entities.snowball_tweaks.mixin.freeze;

import dev.zenfyr.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
abstract class SnowballEntityMixin extends ThrowableItemProjectile {

  public SnowballEntityMixin(
      EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(at = @At("TAIL"), method = "onHitEntity")
  private void andromeda$applyFreezing(EntityHitResult hitResult, CallbackInfo ci) {
    if (hitResult.getEntity().level().isClientSide()) return;

    var config = hitResult.getEntity().level().am$get(Snowballs.CONFIG);
    if (!config.available) return;
    if (!config.freeze) return;

    Entity entity = hitResult.getEntity();
    if (entity instanceof LivingEntity livingEntity) {
      livingEntity.setTicksFrozen(livingEntity.getTicksRequiredToFreeze() + 40);
    }
  }
}
