package me.melontini.andromeda.modules.items.infinite_totem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BeaconUtil {

  public static boolean matchesPattern(Level world, BlockPos pos) {
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();

    for (int j = 1; j <= 4; j++) {
      int k = y - j;
      if (k < world.getMinBuildHeight()) break;

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
