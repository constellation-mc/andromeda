package me.melontini.andromeda.mixin.entities.snowball_tweaks.layers;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.mixin.annotations.ConstructDummy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
@Feature("snowballs.layers")
abstract class SnowballEntityMixin extends ThrownItemEntity {
    @Unique
    private static final Snowballs am$snow = ModuleManager.quick(Snowballs.class);

    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @ConstructDummy(owner = "net.minecraft.class_1297", name = "method_5773", desc = "()V")
    @Inject(at = @At("TAIL"), method = "tick()V")
    public void andromeda$onBlockHit(CallbackInfo ci) {
        if (!am$snow.config().layers || world.isClient()) return;

        Vec3d pos = this.getPos();
        Vec3d vec3d = pos.add(this.getVelocity());
        //We need to recast, since vanilla ignores fluids.
        BlockHitResult hitResult = this.world.raycast(new RaycastContext(pos, vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.WATER, this));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            FluidState fluidState = this.world.getFluidState(blockPos);
            if (fluidState.isEmpty()) {
                BlockState blockState = this.world.getBlockState(blockPos);
                if (!blockState.isAir()) {
                    if (blockState.isOf(Blocks.SNOW)) {
                        int i = blockState.get(SnowBlock.LAYERS);
                        BlockState placedState = i < 7 ? blockState.with(SnowBlock.LAYERS, Math.min(8, i + 1)) : Blocks.SNOW_BLOCK.getDefaultState();
                        this.andromeda$setStateAndDiscard(blockPos, placedState);
                        return;
                    }

                    BlockPos newPos = blockPos.offset(hitResult.getSide());
                    BlockState newBlockState = this.world.getBlockState(newPos);
                    if (newBlockState.isOf(Blocks.SNOW)) {
                        int i = newBlockState.get(SnowBlock.LAYERS);
                        BlockState placedState = i < 7 ? newBlockState.with(SnowBlock.LAYERS, Math.min(8, i + 1)) : Blocks.SNOW_BLOCK.getDefaultState();
                        this.andromeda$setStateAndDiscard(newPos, placedState);
                        return;
                    }
                    if (newBlockState.isAir()) {
                        BlockState below = this.world.getBlockState(newPos.down());
                        if (!below.isAir() && Blocks.SNOW.getDefaultState().canPlaceAt(this.world, newPos)) {
                            this.andromeda$setStateAndDiscard(newPos, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, 1));
                            return;
                        }
                    }
                    this.world.sendEntityStatus(this, (byte)3);
                    this.discard();
                }
            } else {
                this.andromeda$setStateAndDiscard(blockPos, Blocks.ICE.getDefaultState());
            }
        }
    }

    @Unique
    private void andromeda$setStateAndDiscard(BlockPos blockPos, BlockState state) {
        this.world.setBlockState(blockPos, state, Block.NOTIFY_ALL);
        this.world.sendEntityStatus(this, (byte)3);
        this.discard();
    }
}
