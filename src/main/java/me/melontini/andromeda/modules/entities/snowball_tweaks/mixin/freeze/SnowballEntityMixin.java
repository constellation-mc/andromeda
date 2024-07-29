package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.freeze;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
abstract class SnowballEntityMixin extends ThrownItemEntity {

  public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "onEntityHit")
  private void andromeda$applyFreezing(EntityHitResult result, CallbackInfo ci) {
    if (result.getEntity().world.isClient()) return;

    var config = result.getEntity().world.am$get(Snowballs.CONFIG);
    if (!config.available.asBoolean(ConstantLootContextAccessor.get(this))) return;
    if (!config.freeze.asBoolean(
        LootContextUtil.entity(world, result.getEntity().getPos(), result.getEntity(), null, this)))
      return;

    Entity entity = result.getEntity();
    if (entity instanceof LivingEntity livingEntity) {
      livingEntity.setFrozenTicks(livingEntity.getMinFreezeDamageTicks() + 40);
    }
  }
}
