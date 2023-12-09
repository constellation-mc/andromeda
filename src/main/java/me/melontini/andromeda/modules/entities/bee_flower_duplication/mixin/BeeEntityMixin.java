package me.melontini.andromeda.modules.entities.bee_flower_duplication.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.bee_flower_duplication.BeeFlowerDuplication;
import me.melontini.andromeda.modules.misc.unknown.Content;
import me.melontini.andromeda.modules.misc.unknown.Unknown;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeeEntity.class)
abstract class BeeEntityMixin extends AnimalEntity {
    @Unique
    private static final BeeFlowerDuplication am$bfd = ModuleManager.quick(BeeFlowerDuplication.class);

    @Shadow @Nullable BlockPos flowerPos;
    @Shadow BeeEntity.PollinateGoal pollinateGoal;

    @Unique
    private int andromeda$plantingCoolDown;

    protected BeeEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AnimalEntity;tick()V", shift = At.Shift.AFTER), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (!am$bfd.config().enabled) return;

        if (this.andromeda$plantingCoolDown > 0) this.andromeda$plantingCoolDown--;

        if (this.pollinateGoal != null) {
            if (this.pollinateGoal.isRunning() && this.pollinateGoal.completedPollination() && this.andromeda$canPlant()) {
                this.andromeda$growFlower();
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void andromeda$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.andromeda$plantingCoolDown != 0) nbt.putInt("AM-plantingCoolDown", this.andromeda$plantingCoolDown);
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void andromeda$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AM-plantingCoolDown")) this.andromeda$plantingCoolDown = nbt.getInt("AM-plantingCoolDown");
    }

    @Unique
    private void andromeda$growFlower() {
        if (this.flowerPos != null) {
            BlockState flowerState = world.getBlockState(flowerPos);
            if (flowerState.getBlock() instanceof FlowerBlock flowerBlock) {
                andromeda$plantingCoolDown = MathStuff.nextInt(world.random, 3600, 6490);
                for (int i = -2; i <= 2; i++) {
                    for (int b = -2; b <= 2; b++) {
                        for (int c = -2; c <= 2; c++) {
                            BlockPos pos = new BlockPos(flowerPos.getX() + i, flowerPos.getY() + b, flowerPos.getZ() + c);
                            if (world.getBlockState(pos).getBlock() instanceof AirBlock && flowerBlock.canPlaceAt(flowerState, world, pos)) {
                                if (world.random.nextInt(12) == 0) {
                                    if (ModuleManager.get().getModule(Unknown.class).isPresent() && world.random.nextInt(100) == 0) {
                                        world.setBlockState(pos, Content.ROSE_OF_THE_VALLEY_BLOCK.orThrow().getDefaultState());
                                    } else {
                                        world.setBlockState(pos, flowerState);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (flowerState.getBlock() instanceof TallFlowerBlock flowerBlock && am$bfd.config().enabled) {
                andromeda$plantingCoolDown = MathStuff.nextInt(world.random, 3600, 8000);
                for (int i = -1; i <= 1; i++) {
                    for (int b = -2; b <= 2; b++) {
                        for (int c = -1; c <= 1; c++) {
                            BlockPos pos = new BlockPos(flowerPos.getX() + i, flowerPos.getY() + b, flowerPos.getZ() + c);
                            if (world.getBlockState(pos).getBlock() instanceof AirBlock && flowerBlock.canPlaceAt(flowerState, world, pos)) {
                                if (world.random.nextInt(6) == 0) {
                                    TallFlowerBlock.placeAt(world, flowerState, pos, Block.NOTIFY_LISTENERS);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Unique
    private boolean andromeda$canPlant() {
        return this.andromeda$plantingCoolDown == 0;
    }
}
