package dev.zenfyr.andromeda.modules.world.falling_beenests.mixin;

import com.mojang.logging.LogUtils;
import dev.zenfyr.andromeda.common.util.LootContextBuilder;
import dev.zenfyr.andromeda.modules.world.falling_beenests.BeeUtil;
import dev.zenfyr.andromeda.modules.world.falling_beenests.CanBeeNestsFall;
import dev.zenfyr.pulsar.itemstack.ItemStackUtil;
import dev.zenfyr.pulsar.util.PlayerUtil;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
abstract class FallingBlockMixin extends Entity {

  @Shadow
  @Nullable public CompoundTag blockData;

  @Shadow
  private BlockState blockState;

  public FallingBlockMixin(EntityType<?> type, Level world) {
    super(type, world);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
              shift = At.Shift.AFTER),
      method = "tick")
  public void andromeda$tick(CallbackInfo ci) {
    BlockPos blockPos = this.blockPosition();
    BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
    if (blockEntity == null) return;

    if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity
        && this.level.am$get(CanBeeNestsFall.CONFIG).available) {
      if (this.blockState.getBlock() != Blocks.BEE_NEST) return;
      if (blockData == null || !blockData.getBooleanOr("AM-FromFallenBlock", false)) return;

      blockData.putBoolean("AM-FromFallenBlock", false);

      Optional<Player> optional =
          PlayerUtil.findClosestNonCreativePlayerInRange(level, this.blockPosition(), 16);
      final ListTag nbeetlist = blockData.getListOrEmpty("Bees");

      level.destroyBlock(beehiveBlockEntity.getBlockPos(), false);
      for (int i = 0; i < nbeetlist.size(); ++i) {
        CompoundTag entityData = nbeetlist.getCompoundOrEmpty(i).getCompoundOrEmpty("EntityData");
        BeehiveBlockEntity.IGNORED_BEE_TAGS.forEach(entityData::remove);
        Bee bee = EntityType.BEE.create(level, EntitySpawnReason.EVENT);
        if (bee == null) continue;

        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(
            ChunkAccess.problemPath(new ChunkPos(blockPos)), LogUtils.getLogger())) {
          bee.load(TagValueInput.create(scopedCollector, level.registryAccess(), entityData));
        }

        bee.setPos(position());
        bee.setStayOutOfHiveCountdown(400);
        optional.ifPresent(bee::setTarget);
        level.addFreshEntity(bee);
      }
      optional.ifPresent(player -> level
          .getEntitiesOfClass(Bee.class, new AABB(blockPosition()).inflate(50))
          .forEach(bee -> bee.setTarget(player)));

      for (ItemStack stack : LootContextBuilder.prepareLoot(level, BeeUtil.BEE_LOOT_ID)) {
        ItemStackUtil.spawnVelocity(this.position(), stack, level, -0.3, 0.3, 0.05, 0.2, -0.3, 0.3);
      }
    }
  }
}
