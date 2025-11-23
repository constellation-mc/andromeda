package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.freeze;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
abstract class SnowballEntityMixin extends ThrowableItemProjectile {

  public SnowballEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "onHitEntity")
  private void andromeda$applyFreezing(EntityHitResult result, CallbackInfo ci) {
    if (result.getEntity().level.isClientSide()) return;

    var config = result.getEntity().level.am$get(Snowballs.CONFIG);
    if (!config.available.asBoolean(ConstantLootContextAccessor.get(this))) return;
    if (!config.freeze.asBoolean(LootContextBuilder.entity(level, builder -> builder
        .origin(result.getEntity())
        .thisEntity(result.getEntity())
        .genericSource()
        .killer(this)))) return;

    Entity entity = result.getEntity();
    if (entity instanceof LivingEntity livingEntity) {
      livingEntity.setTicksFrozen(livingEntity.getTicksRequiredToFreeze() + 40);
    }
  }
}
