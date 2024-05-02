package me.melontini.andromeda.modules.entities.ghast_tweaks;

import com.google.common.base.Suppliers;
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
                var c = entity.world.am$get(GhastTweaks.CONFIG);
                if (!c.available) return;

                var supplier = Suppliers.memoize(() -> {
                    LootContextParameterSet set = new LootContextParameterSet.Builder((ServerWorld) entity.world)
                            .add(LootContextParameters.ORIGIN, entity.getPos())
                            .add(LootContextParameters.THIS_ENTITY, entity)
                            .build(LootContextTypes.COMMAND);

                    return new LootContext.Builder(set).build(null);
                });

                if (c.explodeOnDeath.asBoolean(supplier)) entity.world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), c.explosionPower.asFloat(supplier), World.ExplosionSourceType.MOB);
            }
        });
    }
}
