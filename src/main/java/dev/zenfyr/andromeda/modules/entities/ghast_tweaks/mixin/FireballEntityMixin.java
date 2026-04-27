package dev.zenfyr.andromeda.modules.entities.ghast_tweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.zenfyr.andromeda.modules.entities.ghast_tweaks.GhastExplosionDuck;
import dev.zenfyr.andromeda.modules.entities.ghast_tweaks.GhastTweaks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
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
                  "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
  private void redirectExplosionType(
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

      if (!instance.isClientSide()) {
        Explosion.BlockInteraction destructionType =
            ((ServerLevel) instance).getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                ? ((ServerLevel) instance)
                        .getGameRules()
                        .getBoolean(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY)
                    ? Explosion.BlockInteraction.DESTROY_WITH_DECAY
                    : Explosion.BlockInteraction.DESTROY
                : Explosion.BlockInteraction.KEEP;

        var pos = new Vec3(x, y, z);
        ServerExplosion explosion = new ServerExplosion(
            ((ServerLevel) instance), entity, null, null, pos, power, createFire, destructionType);
        ((GhastExplosionDuck) explosion).andromeda$convertObsidian(true);
        var i = explosion.explode();

        // TODO: fix this. this was mimicking server explosions by replacing the logic, but there's
        // prolly a safer way to do this.
        // explosion.finalizeExplosion(true);
      }
    } else {
      original.call(instance, entity, x, y, z, power, createFire, explosionSourceType);
    }
  }
}
