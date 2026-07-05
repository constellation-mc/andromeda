package dev.zenfyr.andromeda.modules.entities.slimes.mixin.slowness;

import dev.zenfyr.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
abstract class SlimeEntityMixin extends Mob {

  @Shadow
  public abstract int getSize();

  @Shadow
  protected abstract ParticleOptions getParticleType();

  protected SlimeEntityMixin(EntityType<? extends Mob> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/Mob;push(Lnet/minecraft/world/entity/Entity;)V",
              shift = At.Shift.AFTER),
      method = "push")
  private void andromeda$onEntityCollision(Entity entity, CallbackInfo ci) {
    if (!(entity instanceof LivingEntity target)) return;
    this.andromeda$tryApplyEffect(target);
  }

  @Unique private void andromeda$tryApplyEffect(LivingEntity target) {
    if (level().isClientSide()) return;

    var config = this.level().am$get(Slimes.CONFIG);
    if (!config.available) return;
    if (!config.slowness) return;

    MobEffectInstance effectInstance =
        new MobEffectInstance(MobEffects.SLOWNESS, 20 * this.getSize(), 1, true, false, false);
    target.addEffect(effectInstance);
    if (level().getGameTime() % 3 == 0) {
      ((ServerLevel) level())
          .sendParticles(
              getParticleType(), target.getX(), target.getY(), target.getZ(), 5, 0.2, 0.7, 0.2, 0);
    }
  }
}
