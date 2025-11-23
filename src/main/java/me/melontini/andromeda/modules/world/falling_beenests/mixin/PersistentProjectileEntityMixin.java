package me.melontini.andromeda.modules.world.falling_beenests.mixin;

import me.melontini.andromeda.modules.world.falling_beenests.BeeUtil;
import me.melontini.andromeda.modules.world.falling_beenests.CanBeeNestsFall;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
abstract class PersistentProjectileEntityMixin extends Projectile {

  public PersistentProjectileEntityMixin(EntityType<? extends Projectile> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "onHitBlock")
  private void andromeda$onBeeNestHit(BlockHitResult blockHitResult, CallbackInfo ci) {
    BlockPos pos = blockHitResult.getBlockPos();
    BlockState state = level.getBlockState(pos);

    if (state.getBlock() == Blocks.BEE_NEST && !level.isClientSide()) {
      BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) level.getBlockEntity(pos);
      if (beehiveBlockEntity == null) return;

      if (!level.am$get(CanBeeNestsFall.CONFIG).active) return;

      if (level.getBlockState(pos.relative(Direction.DOWN)).isAir()) {
        BeeUtil.trySpawnFallingBeeNest(level, pos, state, beehiveBlockEntity);
      }
    }
  }
}
