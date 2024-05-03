package me.melontini.andromeda.modules.entities.slimes.mixin.flee;

import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
abstract class MobEntityMixin extends Entity {

    public MobEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=90"), method = "lookAtEntity")
    private float andromeda$rotateSlime(float original, Entity targetEntity, float maxYawChange, float maxPitchChange) {
        if ((MobEntity) (Object) this instanceof SlimeEntity slime && !(targetEntity instanceof SlimeEntity)) {
            var config = world.am$get(Slimes.CONFIG);
            var supplier = Suppliers.memoize(LootContextUtil.command(world, this.getPos(), this));
            if (config.available.asBoolean(supplier) && config.flee && slime.isSmall()) {
                return 270;
            }
        }
        return original;
    }
}
