package me.melontini.andromeda.mixin.entities.slimes.flee;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
@MixinRelatedConfigOption("slimes.flee")
public abstract class MobEntityMixin {

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=90"), method = "lookAtEntity")
    private float andromeda$rotateSlime(float original, Entity targetEntity, float maxYawChange, float maxPitchChange) {
        if ((MobEntity) (Object) this instanceof SlimeEntity slime && !(targetEntity instanceof SlimeEntity)) {
            if (Andromeda.CONFIG.slimes.flee && slime.isSmall()) {
                return 270;
            }
        }
        return original;
    }
}
