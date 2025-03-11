package me.melontini.andromeda.modules.world.falling_beenests;

import java.util.List;
import lombok.NonNull;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.dark_matter.api.data.nbt.NbtBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BeeUtil {

  public static final List<Direction> AROUND_BLOCK_DIRECTIONS =
      List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
  public static final Identifier BEE_LOOT_ID = Andromeda.id("bee_nest/bee_nest_broken");

  public static List<ItemStack> prepareLoot(@NonNull World world, @NonNull Identifier lootId) {
    return ((ServerWorld) world)
        .getServer()
        .getLootManager()
        .getLootTable(lootId)
        .generateLoot(new LootContextParameterSet.Builder(((ServerWorld) world))
            .build(LootContextTypes.EMPTY));
  }

  public static void trySpawnFallingBeeNest(
      @NonNull World world,
      @NonNull BlockPos pos,
      @NonNull BlockState state,
      @NonNull BeehiveBlockEntity beehiveBlockEntity) {
    FallingBlockEntity fallingBlock = new FallingBlockEntity(
        world,
        pos.getX() + 0.5,
        pos.getY(),
        pos.getZ() + 0.5,
        state.contains(Properties.WATERLOGGED)
            ? state.with(Properties.WATERLOGGED, Boolean.FALSE)
            : state);

    // Thanks AccessWidener!
    fallingBlock.readCustomDataFromNbt(NbtBuilder.create()
        .put(
            "TileEntityData",
            NbtBuilder.create()
                .put("Bees", beehiveBlockEntity.getBees())
                .putBoolean("AM-FromFallenBlock", true)
                .build())
        .put("BlockState", NbtHelper.fromBlockState(state))
        .build());

    world.setBlockState(pos, state.getFluidState().getBlockState(), Block.NOTIFY_ALL);
    world.spawnEntity(fallingBlock);
  }
}
