package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.entities.better_furnace_minecart.BetterFurnaceMinecart;
import me.melontini.dark_matter.api.data.nbt.NbtUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.IdentityHashMap;
import java.util.Optional;

public class PlaceBehaviorHandler {

    private static final IdentityHashMap<Item, PlaceBehavior> PLACE_BEHAVIOR_MAP = new IdentityHashMap<>();

    public static void registerPlaceBehavior(Item item, PlaceBehavior placeBehavior) {
        PLACE_BEHAVIOR_MAP.put(item, placeBehavior);
    }

    public static Optional<PlaceBehavior> getPlaceBehavior(Item item) {
        return Optional.ofNullable(PLACE_BEHAVIOR_MAP.get(item));
    }

    public static void init() {
        registerPlaceBehavior(Items.CHEST_MINECART, (stack, world, d, e, f, g, pos) -> {
            ChestMinecartEntity chestMinecart = (ChestMinecartEntity) AbstractMinecartEntity.create(world, d, e + g, f, AbstractMinecartEntity.Type.CHEST, stack, null);
            var nbt = stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
            NbtUtil.readInventoryFromNbt(nbt, chestMinecart, world.getRegistryManager());
            return chestMinecart;
        });

        registerPlaceBehavior(Items.HOPPER_MINECART, (stack, world, d, e, f, g, pos) -> {
            HopperMinecartEntity hopperMinecart = (HopperMinecartEntity) AbstractMinecartEntity.create(world, d, e + g, f, AbstractMinecartEntity.Type.HOPPER, stack, null);
            var nbt = stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
            NbtUtil.readInventoryFromNbt(nbt, hopperMinecart, world.getRegistryManager());
            return hopperMinecart;
        });

        registerPlaceBehavior(Items.FURNACE_MINECART, (stack, world, d, e, f, g, pos) -> {
            FurnaceMinecartEntity furnaceMinecart = (FurnaceMinecartEntity) AbstractMinecartEntity.create(world, d, e + g, f, AbstractMinecartEntity.Type.FURNACE, stack, null);

            var nbt = stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
            furnaceMinecart.fuel = NbtUtil.getInt(nbt, "Fuel", 0, ModuleManager.get().getModule(BetterFurnaceMinecart.class).map(m -> Andromeda.ROOT_HANDLER.get(BetterFurnaceMinecart.CONFIG).maxFuel).orElse(32000));
            furnaceMinecart.pushX = furnaceMinecart.getX() - pos.getX();
            furnaceMinecart.pushZ = furnaceMinecart.getZ() - pos.getZ();

            return furnaceMinecart;
        });
    }

    public interface PlaceBehavior {
        AbstractMinecartEntity dispense(ItemStack stack, ServerWorld world, double d, double e, double f, double g, BlockPos pos);
    }
}
