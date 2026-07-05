package dev.zenfyr.andromeda.modules.misc.unknown;

import dev.zenfyr.pulsar.api.util.MakeSure;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

@UtilityClass
public class UnknownUtil {

  public static final List<Direction> AROUND_BLOCK_DIRECTIONS =
      List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

  public static boolean isClear(Level level, BlockPos pos) {
    if (!level.getBlockState(pos).isAir()) {
      return false;
    }
    for (Direction dir : AROUND_BLOCK_DIRECTIONS) {
      if (!level.getBlockState(pos.relative(dir)).isAir()) {
        return false;
      }
    }
    return true;
  }

  public static Optional<BlockPos> pickRandomSpot(
      @NonNull Level level, @NonNull BlockPos blockPos, int range, @NonNull RandomSource random) {
    MakeSure.isTrue(range > 0, "range can't be negative or zero!");
    double j = (range * range * range) * 0.75;

    while (j > 0) {
      j--;
      var pos = new BlockPos(
          blockPos.getX() + random.nextIntBetweenInclusive(-range, range),
          blockPos.getY() + random.nextIntBetweenInclusive(-range, range),
          blockPos.getZ() + random.nextIntBetweenInclusive(-range, range));
      if (level.getBlockState(pos.above()).isAir()
          && level.getBlockState(pos).isAir()
          && isClear(level, pos)
          && isClear(level, pos.above())) {
        return Optional.of(pos);
      }
    }
    return Optional.empty();
  }
}
