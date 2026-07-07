package dev.zenfyr.andromeda.modules.entities.slimes.mixin.merge;

import dev.zenfyr.andromeda.modules.entities.slimes.SlimeMergeDuck;
import dev.zenfyr.andromeda.modules.entities.slimes.Slimes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
abstract class SlimeMixin extends AbstractCubeMob {

  protected SlimeMixin(EntityType<? extends AbstractCubeMob> type, Level level) {
    super(type, level);
  }

  @Inject(at = @At("TAIL"), method = "addTargetingGoals")
  private void andromeda$newGoal(CallbackInfo ci) {
    Slime self = (Slime) (Object) this;
    this.targetSelector.addGoal(
        2,
        new NearestAttackableTargetGoal<>(
            self, Slime.class, 5, true, false, (livingEntity, level) -> {
              var config = this.level().am$get(Slimes.CONFIG);
              if (!config.available || !config.merge) return false;
              if (((SlimeMergeDuck) this).andromeda$mergeCD() > 0) return false;
              float d = livingEntity.distanceTo(this);
              return d <= 6
                  && (self.getSize() <= config.maxMerge
                      && ((Slime) livingEntity).getSize() < self.getSize());
            }));
  }
}
