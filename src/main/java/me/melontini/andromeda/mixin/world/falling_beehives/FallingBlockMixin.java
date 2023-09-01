package me.melontini.andromeda.mixin.world.falling_beehives;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.ItemStackUtil;
import me.melontini.andromeda.util.WorldUtil;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.minecraft.world.PlayerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(FallingBlockEntity.class)
@MixinRelatedConfigOption("canBeeNestsFall")
public abstract class FallingBlockMixin extends Entity {
    @Shadow
    @Nullable
    public NbtCompound blockEntityData;
    @Shadow
    private BlockState block;

    public FallingBlockMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/world/World.getBlockEntity (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;", shift = At.Shift.AFTER), method = "tick")
    public void andromeda$tick(CallbackInfo ci) {
        if (!Andromeda.CONFIG.canBeeNestsFall) return;

        BlockPos blockPos = this.getBlockPos();
        BlockEntity blockEntity = this.world.getBlockEntity(blockPos);
        if (blockEntity != null) {
            if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
                if (this.block.getBlock() == Blocks.BEE_NEST) {
                    if (blockEntityData != null && blockEntityData.getBoolean("AM-FromFallenBlock")) {
                        blockEntityData.putBoolean("AM-FromFallenBlock", false);
                        List<ItemStack> stacks = WorldUtil.prepareLoot(world, WorldUtil.BEE_LOOT_ID);

                        Optional<PlayerEntity> optional = PlayerUtil.findClosestNonCreativePlayerInRange(world, this.getBlockPos(), 16);
                        final NbtList nbeetlist = blockEntityData.getList("Bees", 10);

                        if (optional.isPresent()) {
                            world.breakBlock(beehiveBlockEntity.getPos(), false);
                            for (int i = 0; i < nbeetlist.size(); ++i) {
                                NbtCompound entityData = nbeetlist.getCompound(i).getCompound("EntityData");
                                BeehiveBlockEntity.removeIrrelevantNbtKeys(entityData);
                                BeeEntity bee = EntityType.BEE.create(world);
                                if (bee != null) {
                                    bee.readNbt(entityData);
                                    bee.setPosition(getPos());
                                    bee.setTarget(optional.get());
                                    world.spawnEntity(bee);
                                }
                            }
                            for (BeeEntity bee : world.getNonSpectatingEntities(BeeEntity.class, new Box(getBlockPos()).expand(50))) {
                                bee.setTarget(optional.get());
                            }
                            for (ItemStack stack : stacks) {
                                ItemStackUtil.spawnVelocity(this.getPos(), stack, world,
                                        -0.3, 0.3, 0.05, 0.2, -0.3, 0.3);
                            }
                        } else {
                            world.breakBlock(beehiveBlockEntity.getPos(), false);
                            for (int i = 0; i < nbeetlist.size(); ++i) {
                                NbtCompound entityData = nbeetlist.getCompound(i).getCompound("EntityData");
                                BeehiveBlockEntity.removeIrrelevantNbtKeys(entityData);
                                BeeEntity bee = EntityType.BEE.create(world);
                                if (bee != null) {
                                    bee.readNbt(entityData);
                                    bee.setPosition(getPos());
                                    bee.setCannotEnterHiveTicks(400);
                                    world.spawnEntity(bee);
                                }
                            }
                            for (ItemStack stack : stacks) {
                                ItemStackUtil.spawnVelocity(this.getPos(), stack, world,
                                        new Vec3d(MathStuff.nextDouble(-.3, .3), MathStuff.nextDouble(.05, .2), MathStuff.nextDouble(-.3, .3)));
                            }
                        }
                    }
                }
            }
        }
    }
}
