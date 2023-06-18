package me.melontini.andromeda.mixin.world.falling_beehives;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.*;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.melontini.andromeda.util.WorldUtil.trySpawnFallingBeeNest;

@Mixin(PersistentProjectileEntity.class)
@MixinRelatedConfigOption("canBeeNestsFall")
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
    public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("TAIL"), method = "onBlockHit")
    private void andromeda$onBeeNestHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        Entity entity = this;
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (entity instanceof ArrowEntity) {
            if (Andromeda.CONFIG.canBeeNestsFall) {
                if (block == Blocks.BEE_NEST) {
                    BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) world.getBlockEntity(pos);
                    if (beehiveBlockEntity != null)
                        if (world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock() instanceof AirBlock) {
                            for (int i = 0; i < 4; i++) {
                                switch (i) {
                                    case 0 -> {
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
}
