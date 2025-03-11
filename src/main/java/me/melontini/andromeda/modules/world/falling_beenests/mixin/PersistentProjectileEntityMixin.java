package me.melontini.andromeda.modules.world.falling_beenests.mixin;

import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.world.falling_beenests.BeeUtil;
import me.melontini.andromeda.modules.world.falling_beenests.CanBeeNestsFall;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
abstract class PersistentProjectileEntityMixin extends ProjectileEntity {

  public PersistentProjectileEntityMixin(
      EntityType<? extends ProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  @Inject(at = @At("TAIL"), method = "onBlockHit")
  private void andromeda$onBeeNestHit(BlockHitResult blockHitResult, CallbackInfo ci) {
    BlockPos pos = blockHitResult.getBlockPos();
    BlockState state = world.getBlockState(pos);

    if (state.getBlock() == Blocks.BEE_NEST && !world.isClient()) {
      BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) world.getBlockEntity(pos);
      if (beehiveBlockEntity == null) return;

      if (!world
          .am$get(CanBeeNestsFall.CONFIG)
          .available
          .asBoolean(LootContextBuilder.block(
              world, builder -> builder.origin(pos).state(state).blockEntity(beehiveBlockEntity))))
        return;

      if (world.getBlockState(pos.offset(Direction.DOWN)).isAir()) {
        BeeUtil.trySpawnFallingBeeNest(world, pos, state, beehiveBlockEntity);
      }
    }
  }
}
