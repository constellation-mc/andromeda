package me.melontini.andromeda.mixin.world.falling_beehives;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.WorldUtil;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.melontini.andromeda.util.WorldUtil.trySpawnFallingBeeNest;

@Mixin(BeehiveBlockEntity.class)
@Feature("canBeeNestsFall")
abstract class BeehiveBlockEntityMixin extends BlockEntity {
    @Unique
    private boolean andromeda$FromFallen;

    public BeehiveBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(at = @At("HEAD"), method = "serverTick")
    private static void andromeda$fallingHive(@NotNull World world, BlockPos pos, BlockState state, BeehiveBlockEntity beehiveBlockEntity, CallbackInfo ci) {
        if (!Config.get().canBeeNestsFall) return;

        if (world.getBlockState(pos).getBlock() == Blocks.BEE_NEST) {
            if (world.random.nextInt(32000) == 0) {
                if (world.getBlockState(pos.offset(Direction.DOWN)).getBlock() instanceof AirBlock) {
                    BlockState up = world.getBlockState(pos.offset(Direction.UP));
                    if (up.isIn(BlockTags.LOGS) || up.isIn(BlockTags.LEAVES)) {
                        for (Direction direction : WorldUtil.AROUND_BLOCK_DIRECTIONS) {
                            if (world.getBlockState(pos.offset(direction)).isIn(BlockTags.LOGS)) {
                                trySpawnFallingBeeNest(world, pos, state, beehiveBlockEntity);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "readNbt")
    private void andromeda$readNbt(@NotNull NbtCompound nbt, CallbackInfo ci) {
        this.andromeda$FromFallen = nbt.getBoolean("AM-FromFallenBlock");
    }

    @Inject(at = @At("TAIL"), method = "writeNbt")
    private void andromeda$writeNbt(@NotNull NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("AM-FromFallenBlock", this.andromeda$FromFallen);
    }
}
