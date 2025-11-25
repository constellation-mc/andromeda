package dev.zenfyr.andromeda.modules.entities.snowball_tweaks.mixin.melt;

import dev.zenfyr.andromeda.modules.entities.snowball_tweaks.Snowballs;
import me.melontini.dark_matter.api.mixin.annotations.ConstructDummy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
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

  // TODO(Ravel): target method tick with the signature not found
  @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
  @ConstructDummy(owner = "net.minecraft.class_1297", name = "method_5773", desc = "()V")
  @Inject(at = @At("HEAD"), method = "tick()V")
  public void andromeda$melt(CallbackInfo ci) {
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
