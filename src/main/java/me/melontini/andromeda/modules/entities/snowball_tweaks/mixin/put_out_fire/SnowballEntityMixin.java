package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.put_out_fire;

import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import me.melontini.dark_matter.api.base.util.MathUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
abstract class SnowballEntityMixin extends ThrowableItemProjectile {

  public SnowballEntityMixin(
      EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "onHitEntity")
  private void andromeda$extinguish(EntityHitResult result, CallbackInfo ci) {
    if (result.getEntity().level.isClientSide()) return;

    var config = result.getEntity().level.am$get(Snowballs.CONFIG);
    if (!config.available) return;
    Entity entity = result.getEntity();
    if (!config.extinguish) return;

    if (entity.isOnFire()) {
      entity.clearFire();
      entity.playSound(
          SoundEvents.GENERIC_EXTINGUISH_FIRE,
          0.7F,
          1.6F
              + (MathUtil.threadRandom().nextFloat() - MathUtil.threadRandom().nextFloat()) * 0.4F);
    }
  }
}
