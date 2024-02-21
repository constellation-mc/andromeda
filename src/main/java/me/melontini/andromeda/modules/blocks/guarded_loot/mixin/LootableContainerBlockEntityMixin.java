package me.melontini.andromeda.modules.blocks.guarded_loot.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.melontini.andromeda.modules.blocks.guarded_loot.Main.*;

@Mixin(LootableContainerBlockEntity.class)
abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity {

    protected LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"), method = "checkUnlocked")
    private boolean lockedIfMonstersNearby(boolean locked, @Local(argsOnly = true) PlayerEntity player) {
        var monsters = checkMonsterLock(player.world, this.getPos());
        if (monsters.isEmpty() || player.getAbilities().creativeMode || checkLockPicking(player)) return locked;

        handleLockedContainer(player, monsters);
        return true;
    }
}
