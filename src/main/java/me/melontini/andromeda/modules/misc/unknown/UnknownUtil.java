package me.melontini.andromeda.modules.misc.unknown;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@UtilityClass
public class UnknownUtil {

  public static final List<Direction> AROUND_BLOCK_DIRECTIONS =
      List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

  public static boolean isClear(World world, BlockPos pos) {
    if (!world.getBlockState(pos).isAir()) {
      return false;
    }
    for (Direction dir : AROUND_BLOCK_DIRECTIONS) {
      if (!world.getBlockState(pos.offset(dir)).isAir()) {
        return false;
      }
    }
    return true;
  }

  public static Optional<BlockPos> pickRandomSpot(
      @NonNull World world, @NonNull BlockPos blockPos, int range, @NonNull Random random) {
    MakeSure.isTrue(range > 0, "range can't be negative or zero!");
    double j = (range * range * range) * 0.75;

    while (j > 0) {
      j--;
      var pos = new BlockPos(
          blockPos.getX() + random.nextBetween(-range, range),
          blockPos.getY() + random.nextBetween(-range, range),
          blockPos.getZ() + random.nextBetween(-range, range));
      if (world.getBlockState(pos.up()).isAir()
          && world.getBlockState(pos).isAir()
          && isClear(world, pos)
          && isClear(world, pos.up())) {
        return Optional.of(pos);
      }
    }
    return Optional.empty();
  }
}
