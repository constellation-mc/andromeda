package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.minecraft.data.NbtBuilder;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class PickUpBehaviorHandler {

    private static final Map<Block, PickUpBehavior> PICK_UP_BEHAVIOR_MAP = new IdentityHashMap<>();

    public static void registerPickUpBehavior(Block block, PickUpBehavior pickUpBehavior) {
        PICK_UP_BEHAVIOR_MAP.put(block, pickUpBehavior);
    }

    public static Optional<PickUpBehavior> getPickUpBehavior(Block block) {
        return Optional.ofNullable(PICK_UP_BEHAVIOR_MAP.get(block));
    }

    public static void init() {
        registerPickUpBehavior(Blocks.CHEST, (state, world, pos) -> {
            ChestBlockEntity chestBlockEntity = (ChestBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
            ItemStack chestMinecart = new ItemStack(Items.CHEST_MINECART, 1);

            chestMinecart.setNbt(NbtUtil.writeInventoryToNbt(new NbtCompound(), chestBlockEntity));
            chestBlockEntity.clear();
            return chestMinecart;
        });

        registerPickUpBehavior(Blocks.TNT, (state, world, pos) -> new ItemStack(Items.TNT_MINECART, 1));

        registerPickUpBehavior(Blocks.FURNACE, (state, world, pos) -> {
            AbstractFurnaceBlockEntity furnaceBlock = (AbstractFurnaceBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
            ItemStack furnaceMinecart = new ItemStack(Items.FURNACE_MINECART, 1);
            //2.25
            furnaceMinecart.setNbt(NbtBuilder.create().putInt("Fuel", (int) (furnaceBlock.burnTime * 2.25)).build());
            return furnaceMinecart;
        });

        registerPickUpBehavior(Blocks.HOPPER, (state, world, pos) -> {
            HopperBlockEntity hopperBlockEntity = (HopperBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
            ItemStack hopperMinecart = new ItemStack(Items.HOPPER_MINECART, 1);

            hopperMinecart.setNbt(NbtUtil.writeInventoryToNbt(new NbtCompound(), hopperBlockEntity));
            hopperBlockEntity.clear();
            return hopperMinecart;
        });
    }

    public interface PickUpBehavior {
        ItemStack pickUp(BlockState state, World world, BlockPos pos);
    }
}
