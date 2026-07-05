package dev.zenfyr.andromeda.modules.entities.zombie.all_pick_up.mixin;

import dev.zenfyr.andromeda.modules.entities.zombie.all_pick_up.Pickup;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
abstract class ZombieMixin extends Monster {

  protected ZombieMixin(EntityType<? extends Monster> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/monster/zombie/Zombie;setCanPickUpLoot(Z)V",
              shift = At.Shift.AFTER),
      method = "finalizeSpawn")
  private void andromeda$initialize(
      ServerLevelAccessor level,
      DifficultyInstance difficulty,
      EntitySpawnReason spawnReason,
      SpawnGroupData groupData,
      CallbackInfoReturnable<SpawnGroupData> cir) {
    if (level.getLevel().am$get(Pickup.CONFIG).available) this.setCanPickUpLoot(true);
  }
}
