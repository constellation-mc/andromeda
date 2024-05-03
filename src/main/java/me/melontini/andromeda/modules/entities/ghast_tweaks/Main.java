package me.melontini.andromeda.modules.entities.ghast_tweaks;

import com.google.common.base.Suppliers;
import me.melontini.andromeda.common.util.LootContextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.world.World;

public class Main {

    Main() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof GhastEntity) {
                var c = entity.world.am$get(GhastTweaks.CONFIG);
                var supplier = Suppliers.memoize(LootContextUtil.command(entity.world, entity.getPos(), entity));
                if (!c.available.asBoolean(supplier)) return;

                if (c.explodeOnDeath.asBoolean(supplier)) entity.world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), c.explosionPower.asFloat(supplier), World.ExplosionSourceType.MOB);
            }
        });
    }
}
