package dev.zenfyr.andromeda.modules.misc.minor_inconvenience.mixin;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.misc.minor_inconvenience.MinorInconvenience;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerEntityMixin extends LivingEntity {

  @Unique private static final ResourceKey<DamageType> AGONY =
      Andromeda.key(Registries.DAMAGE_TYPE, "agony");

  protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/Avatar;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
              shift = At.Shift.BEFORE),
      method = "hurtServer",
      cancellable = true)
  private void andromeda$damage(
      ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    if (!level.isClientSide()
        && !source.is(AGONY)
        && level.am$get(MinorInconvenience.CONFIG).available) {
      DamageSource damageSource = this.level().damageSources().source(AGONY, this);
      super.hurtServer(level, damageSource, Float.MAX_VALUE);
      this.level()
          .explode(
              null,
              damageSource,
              null,
              this.getBlockX() + 0.5,
              this.getBlockY() + 0.5,
              this.getBlockZ() + 0.5,
              5.0F,
              true,
              Level.ExplosionInteraction.MOB);
      cir.setReturnValue(false);
    }
  }
}
