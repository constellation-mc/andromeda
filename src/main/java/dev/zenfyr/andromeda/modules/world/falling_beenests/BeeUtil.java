package dev.zenfyr.andromeda.modules.world.falling_beenests;

import com.mojang.logging.LogUtils;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.pulsar.api.nbt.CompoundTagBuilder;
import java.util.List;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public class BeeUtil {

  public static final List<Direction> AROUND_BLOCK_DIRECTIONS =
      List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
  public static final ResourceKey<LootTable> BEE_LOOT_ID =
      Andromeda.key(Registries.LOOT_TABLE, "bee_nest/bee_nest_broken");
  private static final Logger MOJLOGGER = LogUtils.getLogger();

  public static void trySpawnFallingBeeNest(
      @NonNull Level world,
      @NonNull BlockPos pos,
      @NonNull BlockState state,
      @NonNull BeehiveBlockEntity beehiveBlockEntity) {
    FallingBlockEntity fallingBlock = new FallingBlockEntity(
        world,
        pos.getX() + 0.5,
        pos.getY(),
        pos.getZ() + 0.5,
        state.hasProperty(BlockStateProperties.WATERLOGGED)
            ? state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE)
            : state);

    // Thanks AccessWidener!
    try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(
        ChunkAccess.problemPath(new ChunkPos(pos)), MOJLOGGER)) {
      fallingBlock.readAdditionalSaveData(TagValueInput.create(
          scopedCollector,
          world.registryAccess(),
          CompoundTagBuilder.create()
              .put(
                  "TileEntityData",
                  CompoundTagBuilder.create()
                      .put(
                          "Bees",
                          BeehiveBlockEntity.Occupant.LIST_CODEC
                              .encodeStart(NbtOps.INSTANCE, beehiveBlockEntity.getBees())
                              .getOrThrow())
                      .putBoolean("AM-FromFallenBlock", true)
                      .build())
              .put("BlockState", NbtUtils.writeBlockState(state))
              .build()));
    }

    world.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
    world.addFreshEntity(fallingBlock);
  }
}
