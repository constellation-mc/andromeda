package dev.zenfyr.andromeda.modules.entities.ghast_tweaks;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;

public final class Main {

  static void init() {
    ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
      if (entity instanceof Ghast) {
        var c = entity.level.am$get(GhastTweaks.CONFIG);
        if (!c.available) return;

        if (c.explodeOnDeath)
          entity.level.explode(
              entity,
              entity.getX(),
              entity.getY(),
              entity.getZ(),
              c.explosionPower,
              Level.ExplosionInteraction.MOB);
      }
    });
  }
}
