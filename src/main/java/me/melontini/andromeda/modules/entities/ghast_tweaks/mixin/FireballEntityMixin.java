package me.melontini.andromeda.modules.entities.ghast_tweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.modules.entities.ghast_tweaks.GhastExplosionDuck;
import me.melontini.andromeda.modules.entities.ghast_tweaks.GhastTweaks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LargeFireball.class)
abstract class FireballEntityMixin extends Fireball {

  public FireballEntityMixin(EntityType<? extends Fireball> entityType, Level world) {
    super(entityType, world);
  }

  @WrapOperation(
      method = "onHit",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
  private Explosion redirectExplosionType(
      Level instance,
      Entity entity,
      double x,
      double y,
      double z,
      float power,
      boolean createFire,
      Level.ExplosionInteraction explosionSourceType,
      Operation<Explosion> original) {
    if (this.getOwner() instanceof Ghast
        && instance.am$get(GhastTweaks.CONFIG).fireBallsConvertObsidian) {
      Explosion.BlockInteraction destructionType =
          instance.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
              ? instance.getGameRules().getBoolean(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY)
                  ? Explosion.BlockInteraction.DESTROY_WITH_DECAY
                  : Explosion.BlockInteraction.DESTROY
              : Explosion.BlockInteraction.KEEP;

      Explosion explosion =
          new Explosion(instance, entity, null, null, x, y, z, power, createFire, destructionType);
      ((GhastExplosionDuck) explosion).andromeda$convertObsidian(true);
      explosion.explode();
      explosion.finalizeExplosion(true);
      return explosion;
    } else {
      return original.call(instance, entity, x, y, z, power, createFire, explosionSourceType);
    }
  }
}
