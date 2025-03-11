package me.melontini.andromeda.modules.entities.slimes.mixin.flee;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
abstract class MobEntityMixin extends Entity {

  @Shadow
  @Nullable private LivingEntity target;

  public MobEntityMixin(EntityType<?> type, World world) {
    super(type, world);
  }

  @ModifyExpressionValue(
      at = @At(value = "CONSTANT", args = "floatValue=90"),
      method = "lookAtEntity")
  private float andromeda$rotateSlime(
      float original, Entity target, float maxYawChange, float maxPitchChange) {
    if ((MobEntity) (Object) this instanceof SlimeEntity slime
        && !(target instanceof SlimeEntity)) {
      var config = world.am$get(Slimes.CONFIG);

      if (!slime.isSmall()) return original;
      if (!config.available.asBoolean(ConstantLootContextAccessor.get(this))) return original;

      if (config.flee.asBoolean(LootContextBuilder.entity(
          world, builder -> builder.origin(target).thisEntity(target).genericSource()))) return 270;
    }
    return original;
  }
}
