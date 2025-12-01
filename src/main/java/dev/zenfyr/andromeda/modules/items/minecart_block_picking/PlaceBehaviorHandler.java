package dev.zenfyr.andromeda.modules.items.minecart_block_picking;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.furnace_minecart_tweaks.FurnaceMinecartTweaks;
import dev.zenfyr.pulsar.nbt.NbtUtil;
import java.util.IdentityHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class PlaceBehaviorHandler {

  private static final IdentityHashMap<Item, PlaceBehavior> PLACE_BEHAVIOR_MAP =
      new IdentityHashMap<>();

  public static void registerPlaceBehavior(Item item, PlaceBehavior placeBehavior) {
    PLACE_BEHAVIOR_MAP.put(item, placeBehavior);
  }

  public static Optional<PlaceBehavior> getPlaceBehavior(Item item) {
    return Optional.ofNullable(PLACE_BEHAVIOR_MAP.get(item));
  }

  public static void init() {
    registerPlaceBehavior(Items.CHEST_MINECART, (stack, world, d, e, f, g, pos) -> {
      MinecartChest chestMinecart = (MinecartChest)
          AbstractMinecart.createMinecart(world, d, e + g, f, AbstractMinecart.Type.CHEST);

      NbtUtil.readInventoryFromTag(stack.getTag(), chestMinecart);
      if (stack.hasCustomHoverName()) chestMinecart.setCustomName(stack.getHoverName());
      return chestMinecart;
    });

    registerPlaceBehavior(Items.HOPPER_MINECART, (stack, world, d, e, f, g, pos) -> {
      MinecartHopper hopperMinecart = (MinecartHopper)
          AbstractMinecart.createMinecart(world, d, e + g, f, AbstractMinecart.Type.HOPPER);

      NbtUtil.readInventoryFromTag(stack.getTag(), hopperMinecart);
      if (stack.hasCustomHoverName()) hopperMinecart.setCustomName(stack.getHoverName());
      return hopperMinecart;
    });

    registerPlaceBehavior(Items.FURNACE_MINECART, (stack, world, d, e, f, g, pos) -> {
      MinecartFurnace furnaceMinecart = (MinecartFurnace)
          AbstractMinecart.createMinecart(world, d, e + g, f, AbstractMinecart.Type.FURNACE);

      furnaceMinecart.fuel = NbtUtil.getInt(
          stack.getTag(),
          "Fuel",
          0,
          ModuleManager.get()
              .get(FurnaceMinecartTweaks.class)
              .map(m -> Andromeda.MAIN.get(FurnaceMinecartTweaks.CONFIG).maxFuel)
              .orElse(32000));
      if (furnaceMinecart.fuel > 0) {
        furnaceMinecart.xPush = furnaceMinecart.getX() - pos.getX();
        furnaceMinecart.zPush = furnaceMinecart.getZ() - pos.getZ();
      }
      if (stack.hasCustomHoverName()) furnaceMinecart.setCustomName(stack.getHoverName());

      return furnaceMinecart;
    });
  }

  public interface PlaceBehavior {
    AbstractMinecart dispense(
        ItemStack stack, Level world, double d, double e, double f, double g, BlockPos pos);
  }
}
