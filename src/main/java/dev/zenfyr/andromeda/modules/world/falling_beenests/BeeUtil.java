package dev.zenfyr.andromeda.modules.world.falling_beenests;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.pulsar.nbt.NbtBuilder;
import java.util.List;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class BeeUtil {

  public static final List<Direction> AROUND_BLOCK_DIRECTIONS =
      List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
  public static final ResourceLocation BEE_LOOT_ID = Andromeda.id("bee_nest/bee_nest_broken");

  public static List<ItemStack> prepareLoot(
      @NonNull Level world, @NonNull ResourceLocation lootId) {
    return ((ServerLevel) world)
        .getServer()
        .getLootData()
        .getLootTable(lootId)
        .getRandomItems(
            new LootParams.Builder(((ServerLevel) world)).create(LootContextParamSets.EMPTY));
  }

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
    fallingBlock.readAdditionalSaveData(NbtBuilder.create()
        .put(
            "TileEntityData",
            NbtBuilder.create()
                .put("Bees", beehiveBlockEntity.writeBees())
                .putBoolean("AM-FromFallenBlock", true)
                .build())
        .put("BlockState", NbtUtils.writeBlockState(state))
        .build());

    world.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
    world.addFreshEntity(fallingBlock);
  }
}
