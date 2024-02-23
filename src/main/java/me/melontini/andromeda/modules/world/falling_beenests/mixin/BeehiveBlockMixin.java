package me.melontini.andromeda.modules.world.falling_beenests.mixin;

import me.melontini.andromeda.common.util.WorldUtil;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeehiveBlock.class)
abstract class BeehiveBlockMixin {

    @Inject(at = @At("HEAD"), method = "getStateForNeighborUpdate", cancellable = true)
    private void andromeda$checkSupport(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (!(world instanceof World)) return;
        for (Direction value : Direction.values()) {
            if (!world.getBlockState(pos.offset(value)).isAir()) {
                return;
            }
        }
        WorldUtil.trySpawnFallingBeeNest((World) world, pos, state, (BeehiveBlockEntity) world.getBlockEntity(pos));
        cir.setReturnValue(state.getFluidState().getBlockState());
    }
}
