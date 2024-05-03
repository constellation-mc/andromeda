package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.put_out_fire;

import com.google.common.base.Suppliers;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import me.melontini.dark_matter.api.base.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
abstract class SnowballEntityMixin extends ThrownItemEntity {

    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "onEntityHit")
    private void andromeda$extinguish(EntityHitResult result, CallbackInfo ci) {
        if (result.getEntity().world.isClient()) return;

        var config = result.getEntity().world.am$get(Snowballs.CONFIG);
        var supplier = Suppliers.memoize(LootContextUtil.entity(world, result.getEntity().getPos(), result.getEntity(), null, this));
        if (!config.available.asBoolean(supplier) || !config.extinguish) return;

        Entity entity = result.getEntity();
        if (entity.isOnFire()) {
            entity.extinguish();
            entity.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (MathUtil.threadRandom().nextFloat() - MathUtil.threadRandom().nextFloat()) * 0.4F);
        }
    }
}
