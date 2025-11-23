package me.melontini.andromeda.modules.entities.zombie.all_pick_up.mixin;

import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.modules.entities.zombie.all_pick_up.Pickup;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
abstract class ZombieEntityMixin extends Monster {

  protected ZombieEntityMixin(EntityType<? extends Monster> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/monster/Zombie;setCanPickUpLoot(Z)V",
              shift = At.Shift.AFTER),
      method = "finalizeSpawn")
  private void andromeda$initialize(
          ServerLevelAccessor world,
          DifficultyInstance difficulty,
          MobSpawnType spawnReason,
          SpawnGroupData entityData,
          CompoundTag entityNbt,
          CallbackInfoReturnable<SpawnGroupData> cir) {
    if (world.isClientSide()) return;

    if (world
        .getLevel()
        .am$get(Pickup.CONFIG)
        .available
        .asBoolean(ConstantLootContextAccessor.get(this))) this.setCanPickUpLoot(true);
  }
}
