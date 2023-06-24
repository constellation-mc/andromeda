package me.melontini.andromeda.mixin.entities.slimes.flee;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SlimeEntity.FaceTowardTargetGoal.class)
@MixinRelatedConfigOption("slimes.flee")
public abstract class FaceTowardTargetGoalMixin extends Goal {
    @Shadow
    @Final
    private SlimeEntity slime;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SlimeEntity;lookAtEntity(Lnet/minecraft/entity/Entity;FF)V"), method = "tick")
    private void andromeda$rotate(SlimeEntity entity, Entity targetEntity, float maxYawChange, float maxPitchChange, Operation<Void> o) {
        if (slime.isSmall() && !(targetEntity instanceof SlimeEntity) && Andromeda.CONFIG.slimes.flee) {
            double d = targetEntity.getX() - slime.getX();
            double e = targetEntity.getZ() - slime.getZ();
            double f;
            if (targetEntity instanceof LivingEntity livingEntity) {
                f = livingEntity.getEyeY() - slime.getEyeY();
            } else {
                f = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0 - slime.getEyeY();
            }

            double g = Math.sqrt(d * d + e * e);
            float h = (float) (MathHelper.atan2(e, d) * 180.0F / (float) Math.PI) - 270.0F;
            float i = (float) (-(MathHelper.atan2(f, g) * 180.0F / (float) Math.PI));
            slime.setPitch(slime.changeAngle(slime.getPitch(), i, maxPitchChange));
            slime.setYaw(slime.changeAngle(slime.getYaw(), h, maxYawChange));
            return;
        }
        o.call(entity, targetEntity, maxYawChange, maxPitchChange);
    }
}
