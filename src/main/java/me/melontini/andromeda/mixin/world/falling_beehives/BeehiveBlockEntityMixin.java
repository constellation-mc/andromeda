package me.melontini.andromeda.mixin.world.falling_beehives;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.*;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.melontini.andromeda.util.WorldUtil.trySpawnFallingBeeNest;

@Mixin(BeehiveBlockEntity.class)
@MixinRelatedConfigOption("canBeeNestsFall")
public abstract class BeehiveBlockEntityMixin extends BlockEntity {
    private boolean andromeda$FromFallen;

    public BeehiveBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(at = @At("HEAD"), method = "serverTick")
    private static void andromeda$fallingHive(@NotNull World world, BlockPos pos, BlockState state, BeehiveBlockEntity beehiveBlockEntity, CallbackInfo ci) {
        if (Andromeda.CONFIG.canBeeNestsFall) {
            if (world.getBlockState(pos).getBlock() == Blocks.BEE_NEST) {
                if (world.random.nextInt(32000) == 0) {
                    if (world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock() instanceof AirBlock) {
                        for (int i = 0; i < 4; i++) {
                            switch (i) {
                                case 0 -> {
                                    //I've run out of ways to check if the bee nest is on a tree.
                                    if (world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ())).getBlock() instanceof PillarBlock)
                                        if (world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ())).getMaterial() == Material.WOOD)
                                            trySpawnFallingBeeNest(world, pos, state, beehiveBlockEntity);
                                }
                                case 1 -> {
                                    if (world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ())).getBlock() instanceof PillarBlock)
                                        if (world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ())).getMaterial() == Material.WOOD)
                                            trySpawnFallingBeeNest(world, pos, state, beehiveBlockEntity);
                                }
                                case 2 -> {
                                    if (world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1)).getBlock() instanceof PillarBlock)
                                        if (world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1)).getMaterial() == Material.WOOD)
                                            trySpawnFallingBeeNest(world, pos, state, beehiveBlockEntity);
                                }
                                case 3 -> {
                                    if (world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1)).getBlock() instanceof PillarBlock)
                                        if (world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1)).getMaterial() == Material.WOOD)
                                            trySpawnFallingBeeNest(world, pos, state, beehiveBlockEntity);
                                }
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
