package dev.zenfyr.andromeda.modules.entities.slimes.mixin.flee;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
abstract class MobEntityMixin extends Entity {

  @Shadow
  @Nullable private LivingEntity target;

  public MobEntityMixin(EntityType<?> type, Level world) {
    super(type, world);
  }

  @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=90"), method = "lookAt")
  private float andromeda$rotateSlime(float original, Entity entity, float yMax, float xMax) {
    if ((Mob) (Object) this instanceof Slime slime && !(entity instanceof Slime)) {
      var config = level().am$get(Slimes.CONFIG);

      if (!slime.isTiny()) return original;
      if (!config.available) return original;

      if (config.flee) return 270;
    }
    return original;
  }
}
