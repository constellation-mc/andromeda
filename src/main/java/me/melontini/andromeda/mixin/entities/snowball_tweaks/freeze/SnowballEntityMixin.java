package me.melontini.andromeda.mixin.entities.snowball_tweaks.freeze;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
@MixinRelatedConfigOption("snowballs.freeze")
public class SnowballEntityMixin {
    @Inject(at = @At("TAIL"), method = "onEntityHit")
    private void andromeda$applyFreezing(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (Andromeda.CONFIG.snowballs.freeze) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setFrozenTicks(livingEntity.getMinFreezeDamageTicks() + 40);
            }
        }
    }
}
