package me.melontini.andromeda.modules.entities.ghast_tweaks;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.world.World;

public final class Main {

    static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof GhastEntity) {
                var c = entity.world.am$get(GhastTweaks.CONFIG);
                var supplier = ConstantLootContextAccessor.get(entity);
                if (!c.available.asBoolean(supplier)) return;

                if (c.explodeOnDeath.asBoolean(supplier)) entity.world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), c.explosionPower.asFloat(supplier), World.ExplosionSourceType.MOB);
            }
        });
    }
}
