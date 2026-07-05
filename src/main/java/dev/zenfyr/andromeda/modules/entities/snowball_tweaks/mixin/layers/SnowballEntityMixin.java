package dev.zenfyr.andromeda.modules.entities.snowball_tweaks.mixin.layers;

import dev.zenfyr.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrowableProjectile.class)
abstract class SnowballEntityMixin extends Projectile {

  public SnowballEntityMixin(EntityType<? extends Projectile> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(at = @At("TAIL"), method = "tick()V")
  public void andromeda$onBlockHit(CallbackInfo ci) {
    if (!((ThrowableProjectile) (Object) this instanceof Snowball)) return;
    if (level().isClientSide()) return;

    var config = level().am$get(Snowballs.CONFIG);
    if (!config.available || !config.layers) return;

    Vec3 pos = this.position();
    Vec3 vec3d = pos.add(this.getDeltaMovement());
    // We need to recast, since vanilla ignores fluids.
    BlockHitResult hitResult = this.level()
        .clip(
            new ClipContext(pos, vec3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, this));

    if (hitResult.getType() == HitResult.Type.BLOCK) {
      BlockPos blockPos = hitResult.getBlockPos();
      FluidState fluidState = this.level().getFluidState(blockPos);
      if (fluidState.isEmpty()) {
        BlockState blockState = this.level().getBlockState(blockPos);
        if (!blockState.isAir()) {
          if (blockState.is(Blocks.SNOW)) {
            int i = blockState.getValue(SnowLayerBlock.LAYERS);
            BlockState placedState = i < 7
                ? blockState.setValue(SnowLayerBlock.LAYERS, Math.min(8, i + 1))
                : Blocks.SNOW_BLOCK.defaultBlockState();
            this.andromeda$setStateAndDiscard(blockPos, placedState);
            return;
          }

          BlockPos newPos = blockPos.relative(hitResult.getDirection());
          BlockState newBlockState = this.level().getBlockState(newPos);
          if (newBlockState.is(Blocks.SNOW)) {
            int i = newBlockState.getValue(SnowLayerBlock.LAYERS);
            BlockState placedState = i < 7
                ? newBlockState.setValue(SnowLayerBlock.LAYERS, Math.min(8, i + 1))
                : Blocks.SNOW_BLOCK.defaultBlockState();
            this.andromeda$setStateAndDiscard(newPos, placedState);
            return;
          }
          if (newBlockState.isAir()) {
            BlockState below = this.level().getBlockState(newPos.below());
            if (!below.isAir()
                && Blocks.SNOW.defaultBlockState().canSurvive(this.level(), newPos)) {
              this.andromeda$setStateAndDiscard(
                  newPos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
              return;
            }
          }
          this.level().broadcastEntityEvent(this, (byte) 3);
          this.discard();
        }
      } else {
        this.andromeda$setStateAndDiscard(blockPos, Blocks.ICE.defaultBlockState());
      }
    }
  }

  @Unique private void andromeda$setStateAndDiscard(BlockPos blockPos, BlockState state) {
    this.level().setBlock(blockPos, state, Block.UPDATE_ALL);
    this.level().broadcastEntityEvent(this, (byte) 3);
    this.discard();
  }
}
