package me.melontini.andromeda.util;

import net.minecraft.block.Block;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class BlockUtil {
    public static final IntProperty WATER_LEVEL_3 = IntProperty.of("water_level", 1, 3);

    public static int getLevelFromBlocks(World world, BlockPos pos, List<Block> allowedBlocks) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int i = 0;

        for (int j = 1; j <= 4; i = j++) {
            int k = y - j;
            if (k < world.getBottomY()) {
                break;
            }

            boolean bl = true;

            for (int l = x - j; l <= x + j && bl; ++l) {
                for (int m = z - j; m <= z + j; ++m) {
                    if (!allowedBlocks.contains(world.getBlockState(new BlockPos(l, k, m)).getBlock())) {
                        bl = false;
                        break;
                    }
                }
            }

            if (!bl) {
                break;
            }
        }

        return i;
    }
}
