package me.melontini.andromeda.modules.entities.ghast_tweaks;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;

public final class Main {

  static void init() {
    ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
      if (entity instanceof Ghast) {
        var c = entity.level.am$get(GhastTweaks.CONFIG);
        var supplier = ConstantLootContextAccessor.get(entity);
        if (!c.available.asBoolean(supplier)) return;

        if (c.explodeOnDeath.asBoolean(supplier))
          entity.level.explode(
              entity,
              entity.getX(),
              entity.getY(),
              entity.getZ(),
              c.explosionPower.asFloat(supplier),
              Level.ExplosionInteraction.MOB);
      }
    });
  }
}
