package dev.zenfyr.andromeda.modules.blocks.guarded_loot.mixin;

import static dev.zenfyr.andromeda.modules.blocks.guarded_loot.GuardedLootMain.*;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomizableContainerBlockEntity.class)
abstract class LootableContainerBlockEntityMixin extends BaseContainerBlockEntity {

  protected LootableContainerBlockEntityMixin(
      BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
    super(blockEntityType, blockPos, blockState);
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"),
      method = "canOpen")
  private boolean lockedIfMonstersNearby(boolean locked, @Local(argsOnly = true) Player player) {
    var monsters =
        checkMonsterLock(player.level, this.getBlockState(), player, this.getBlockPos(), this);
    if (monsters.isEmpty() || player.getAbilities().instabuild || checkLockPicking(this, player))
      return locked;

    handleLockedContainer(player, this.getBlockPos().getCenter(), monsters);
    return true;
  }
}
