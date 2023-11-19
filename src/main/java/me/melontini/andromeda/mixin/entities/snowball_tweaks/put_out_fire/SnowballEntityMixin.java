package me.melontini.andromeda.mixin.entities.snowball_tweaks.put_out_fire;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
@Feature("snowballs.extinguish")
class SnowballEntityMixin {
    @Unique
    private static final Snowballs am$snow = ModuleManager.quick(Snowballs.class);
    @Inject(at = @At("TAIL"), method = "onEntityHit")
    private void andromeda$extinguish(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!am$snow.config().extinguish) return;

        Entity entity = entityHitResult.getEntity();
        if (entity.isOnFire()) {
            entity.extinguish();
            entity.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (MathStuff.threadRandom().nextFloat() - MathStuff.threadRandom().nextFloat()) * 0.4F);
        }
    }
}
