package me.melontini.andromeda.modules.items.infinite_totem;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BeaconUtil {

  public static boolean matchesPattern(World world, BlockPos pos) {
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();

    for (int j = 1; j <= 4; j++) {
      int k = y - j;
      if (k < world.getBottomY()) break;

      Block current = j % 2 == 0 ? Blocks.DIAMOND_BLOCK : Blocks.NETHERITE_BLOCK;

      for (int l = x - j; l <= x + j; ++l) {
        for (int m = z - j; m <= z + j; ++m) {
          if (world.getBlockState(new BlockPos(l, k, m)).getBlock() != current) {
            return false;
          }
        }
      }
    }

    return true;
  }
}
