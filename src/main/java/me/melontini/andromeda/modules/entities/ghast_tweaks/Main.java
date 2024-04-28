package me.melontini.andromeda.modules.entities.ghast_tweaks;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class Main {

    Main(GhastTweaks module) {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof GhastEntity) {
                var c = entity.world.am$get(module);
                if (c.c.explodeOnDeath) entity.world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), c.c.explosionPower.asFloat(() -> {
                    LootContextParameterSet set = new LootContextParameterSet.Builder((ServerWorld) entity.world)
                            .add(LootContextParameters.ORIGIN, entity.getPos())
                            .add(LootContextParameters.THIS_ENTITY, entity)
                            .build(LootContextTypes.COMMAND);

                    return new LootContext.Builder(set).build(null);
                }), World.ExplosionSourceType.MOB);
            }
        });
    }
}
