package dev.zenfyr.andromeda.modules.entities.snowball_tweaks.mixin.melt;

import dev.zenfyr.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrowableProjectile.class)
abstract class SnowballEntityMixin extends Projectile {

  public SnowballEntityMixin(EntityType<? extends Projectile> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(at = @At("HEAD"), method = "tick()V")
  public void andromeda$melt(CallbackInfo ci) {
    if (!((ThrowableProjectile) (Object) this instanceof Snowball)) return;
    if (level.isClientSide() || !this.isOnFire()) return;

    var config = level.am$get(Snowballs.CONFIG);
    if (!config.available || !config.melt) return;

    ((ServerLevel) level)
        .sendParticles(
            ParticleTypes.FALLING_WATER,
            this.getX(),
            this.getY(),
            this.getZ(),
            10,
            0.5,
            0.5,
            0.5,
            0.4);
    this.discard();
  }
}
