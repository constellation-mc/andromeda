package dev.zenfyr.andromeda.modules.mechanics.dragon_fight.mixin;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.mechanics.dragon_fight.DragonFight;
import dev.zenfyr.andromeda.modules.mechanics.dragon_fight.EnderDragonManager;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystal.class)
abstract class EndCrystalMixin extends Entity {

  public EndCrystalMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V",
              shift = At.Shift.BEFORE),
      method = "hurtServer")
  private void andromeda$damage(
      ServerLevel level, DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
    if (!Andromeda.MAIN.get(DragonFight.CONFIG).respawnCrystals) return;
    EndCrystal self = (EndCrystal) (Object) this;

    if (level.dimension() == Level.END && !level.getDragons().isEmpty() && self.showsBottom()) {
      if (this.position().y() <= 71) return;
      level
          .getAttachedOrCreate(EnderDragonManager.ATTACHMENT.get())
          .queueRespawn(new MutableInt(MathUtil.nextInt(1900, 3500)), this.position());
    }
  }
}
