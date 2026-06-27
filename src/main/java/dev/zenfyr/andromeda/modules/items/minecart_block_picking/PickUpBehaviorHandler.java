package dev.zenfyr.andromeda.modules.items.minecart_block_picking;

import dev.zenfyr.pulsar.api.nbt.CompoundTagBuilder;
import dev.zenfyr.pulsar.api.nbt.NbtUtil;
import dev.zenfyr.pulsar.api.util.MakeSure;
import java.util.IdentityHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
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

      var nbt = CompoundTagBuilder.create(
              NbtUtil.writeInventoryToTag(new CompoundTag(), chestBlockEntity))
          .putString(
              "id",
              BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.CHEST_MINECART).toString())
          .build();
      chestMinecart.set(
          DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.CHEST_MINECART, nbt));
      chestBlockEntity.clearContent();
      return chestMinecart;
    });

    registerPickUpBehavior(Blocks.TNT, (state, world, pos) -> new ItemStack(Items.TNT_MINECART, 1));

    registerPickUpBehavior(Blocks.FURNACE, (state, world, pos) -> {
      AbstractFurnaceBlockEntity furnaceBlock = (AbstractFurnaceBlockEntity) MakeSure.notNull(
          world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
      ItemStack furnaceMinecart = new ItemStack(Items.FURNACE_MINECART, 1);
      // 2.25
      var nbt = CompoundTagBuilder.create()
          .putInt("Fuel", (int) (furnaceBlock.litTimeRemaining * 2.25))
          .putString(
              "id",
              BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.FURNACE_MINECART).toString())
          .build();
      furnaceMinecart.set(
          DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.FURNACE_MINECART, nbt));
      return furnaceMinecart;
    });

    registerPickUpBehavior(Blocks.HOPPER, (state, world, pos) -> {
      HopperBlockEntity hopperBlockEntity = (HopperBlockEntity) MakeSure.notNull(
          world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
      ItemStack hopperMinecart = new ItemStack(Items.HOPPER_MINECART, 1);

      var nbt = CompoundTagBuilder.create(
              NbtUtil.writeInventoryToTag(new CompoundTag(), hopperBlockEntity))
          .putString(
              "id",
              BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HOPPER_MINECART).toString())
          .build();
      hopperMinecart.set(
          DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.HOPPER_MINECART, nbt));
      hopperBlockEntity.clearContent();
      return hopperMinecart;
    });
  }

  public interface PickUpBehavior {
    @Nullable ItemStack pickUp(BlockState state, Level world, BlockPos pos);
  }
}
