package me.melontini.andromeda.modules.world.falling_beenests.mixin;

import me.melontini.andromeda.common.util.ItemStackUtil;
import me.melontini.andromeda.common.util.WorldUtil;
import me.melontini.andromeda.modules.world.falling_beenests.CanBeeNestsFall;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(FallingBlockEntity.class)
abstract class FallingBlockMixin extends Entity {

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
        BlockPos blockPos = this.getBlockPos();
        BlockEntity blockEntity = this.world.getBlockEntity(blockPos);
        if (blockEntity == null) return;

        if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity && this.world.am$get(CanBeeNestsFall.class).enabled) {
            if (this.block.getBlock() != Blocks.BEE_NEST) return;
            if (blockEntityData == null || !blockEntityData.getBoolean("AM-FromFallenBlock")) return;

            blockEntityData.putBoolean("AM-FromFallenBlock", false);

            Optional<PlayerEntity> optional = PlayerUtil.findClosestNonCreativePlayerInRange(world, this.getBlockPos(), 16);
            final NbtList nbeetlist = blockEntityData.getList("Bees", 10);

            world.breakBlock(beehiveBlockEntity.getPos(), false);
            for (int i = 0; i < nbeetlist.size(); ++i) {
                NbtCompound entityData = nbeetlist.getCompound(i).getCompound("EntityData");
                BeehiveBlockEntity.removeIrrelevantNbtKeys(entityData);
                BeeEntity bee = EntityType.BEE.create(world);
                if (bee == null) continue;

                bee.readNbt(entityData);
                bee.setPosition(getPos());
                bee.setCannotEnterHiveTicks(400);
                optional.ifPresent(bee::setTarget);
                world.spawnEntity(bee);
            }
            optional.ifPresent(player -> world.getNonSpectatingEntities(BeeEntity.class, new Box(getBlockPos()).expand(50))
                    .forEach(bee -> bee.setTarget(player)));

            for (ItemStack stack : WorldUtil.prepareLoot(world, WorldUtil.BEE_LOOT_ID)) {
                ItemStackUtil.spawnVelocity(this.getPos(), stack, world, -0.3, 0.3, 0.05, 0.2, -0.3, 0.3);
            }
        }
    }
}
