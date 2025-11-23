package me.melontini.andromeda.modules.entities.slimes.mixin.flee;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
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
  private float andromeda$rotateSlime(
      float original, Entity target, float maxYawChange, float maxPitchChange) {
    if ((Mob) (Object) this instanceof Slime slime && !(target instanceof Slime)) {
      var config = level.am$get(Slimes.CONFIG);

      if (!slime.isTiny()) return original;
      if (!config.available.asBoolean(ConstantLootContextAccessor.get(this))) return original;

      if (config.flee.asBoolean(LootContextBuilder.entity(
          level, builder -> builder.origin(target).thisEntity(target).genericSource()))) return 270;
    }
    return original;
  }
}
