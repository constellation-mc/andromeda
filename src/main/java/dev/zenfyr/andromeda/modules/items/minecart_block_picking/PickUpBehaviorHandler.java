package dev.zenfyr.andromeda.modules.items.minecart_block_picking;

import dev.zenfyr.pulsar.nbt.NbtBuilder;
import dev.zenfyr.pulsar.nbt.NbtUtil;
import dev.zenfyr.pulsar.util.MakeSure;
import java.util.IdentityHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PickUpBehaviorHandler {

  private static final IdentityHashMap<Block, PickUpBehavior> PICK_UP_BEHAVIOR_MAP =
      new IdentityHashMap<>();

  public static void registerPickUpBehavior(Block block, PickUpBehavior pickUpBehavior) {
    PICK_UP_BEHAVIOR_MAP.put(block, pickUpBehavior);
  }

  public static Optional<PickUpBehavior> getPickUpBehavior(Block block) {
    return Optional.ofNullable(PICK_UP_BEHAVIOR_MAP.get(block));
  }

  public static void init() {
    registerPickUpBehavior(Blocks.CHEST, (state, world, pos) -> {
      ChestBlockEntity chestBlockEntity = (ChestBlockEntity) MakeSure.notNull(
          world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
      ItemStack chestMinecart = new ItemStack(Items.CHEST_MINECART, 1);

      chestMinecart.setTag(NbtUtil.writeInventoryToNbt(new CompoundTag(), chestBlockEntity));
      chestBlockEntity.clearContent();
      return chestMinecart;
    });

    registerPickUpBehavior(Blocks.TNT, (state, world, pos) -> new ItemStack(Items.TNT_MINECART, 1));

    registerPickUpBehavior(Blocks.FURNACE, (state, world, pos) -> {
      AbstractFurnaceBlockEntity furnaceBlock = (AbstractFurnaceBlockEntity) MakeSure.notNull(
          world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
      ItemStack furnaceMinecart = new ItemStack(Items.FURNACE_MINECART, 1);
      // 2.25
      furnaceMinecart.setTag(NbtBuilder.create()
          .putInt("Fuel", (int) (furnaceBlock.litTime * 2.25))
          .build());
      return furnaceMinecart;
    });

    registerPickUpBehavior(Blocks.HOPPER, (state, world, pos) -> {
      HopperBlockEntity hopperBlockEntity = (HopperBlockEntity) MakeSure.notNull(
          world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
      ItemStack hopperMinecart = new ItemStack(Items.HOPPER_MINECART, 1);

      hopperMinecart.setTag(NbtUtil.writeInventoryToNbt(new CompoundTag(), hopperBlockEntity));
      hopperBlockEntity.clearContent();
      return hopperMinecart;
    });
  }

  public interface PickUpBehavior {
    @Nullable ItemStack pickUp(BlockState state, Level world, BlockPos pos);
  }
}
