package me.melontini.andromeda.modules.entities.slimes.mixin.flee;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
abstract class MobEntityMixin {
    @Unique
    private static final Slimes am$slimes = ModuleManager.quick(Slimes.class);

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=90"), method = "lookAtEntity")
    private float andromeda$rotateSlime(float original, Entity targetEntity, float maxYawChange, float maxPitchChange) {
        if ((MobEntity) (Object) this instanceof SlimeEntity slime && !(targetEntity instanceof SlimeEntity)) {
            if (am$slimes.config().flee && slime.isSmall()) {
                return 270;
            }
        }
        return original;
    }
}
