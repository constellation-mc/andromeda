package me.melontini.andromeda.mixin.world.falling_beenests;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.WorldUtil;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.melontini.andromeda.util.WorldUtil.trySpawnFallingBeeNest;

@Mixin(PersistentProjectileEntity.class)
@Feature("canBeeNestsFall")
abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
    public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("TAIL"), method = "onBlockHit")
    private void andromeda$onBeeNestHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (!Config.get().canBeeNestsFall) return;

        Entity entity = this;
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (entity instanceof ArrowEntity) {
            if (block == Blocks.BEE_NEST) {
                BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) world.getBlockEntity(pos);
                if (beehiveBlockEntity != null) {
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
    }
}
