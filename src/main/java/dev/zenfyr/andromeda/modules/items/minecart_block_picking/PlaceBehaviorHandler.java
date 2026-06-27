package dev.zenfyr.andromeda.modules.items.minecart_block_picking;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.furnace_minecart_tweaks.FurnaceMinecartTweaks;
import dev.zenfyr.pulsar.api.nbt.NbtUtil;
import dev.zenfyr.pulsar.api.util.MathUtil;
import java.util.IdentityHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
      MinecartChest chestMinecart = AbstractMinecart.createMinecart(
          world, d, e + g, f, EntityTypes.CHEST_MINECART, EntitySpawnReason.DISPENSER, stack, null);

      var nbt = stack.get(DataComponents.ENTITY_DATA);
      if (nbt != null) NbtUtil.readInventoryFromTag(nbt.copyTagWithoutId(), chestMinecart);
      return chestMinecart;
    });

    registerPlaceBehavior(Items.HOPPER_MINECART, (stack, world, d, e, f, g, pos) -> {
      MinecartHopper hopperMinecart = AbstractMinecart.createMinecart(
          world,
          d,
          e + g,
          f,
          EntityTypes.HOPPER_MINECART,
          EntitySpawnReason.DISPENSER,
          stack,
          null);

      var nbt = stack.get(DataComponents.ENTITY_DATA);
      if (nbt != null) NbtUtil.readInventoryFromTag(nbt.copyTagWithoutId(), hopperMinecart);
      return hopperMinecart;
    });

    registerPlaceBehavior(Items.FURNACE_MINECART, (stack, world, d, e, f, g, pos) -> {
      MinecartFurnace furnaceMinecart = AbstractMinecart.createMinecart(
          world,
          d,
          e + g,
          f,
          EntityTypes.FURNACE_MINECART,
          EntitySpawnReason.DISPENSER,
          stack,
          null);

      var nbt = stack.get(DataComponents.ENTITY_DATA);
      if (nbt != null) {
        furnaceMinecart.fuel = MathUtil.clamp(
            NbtUtil.getInt(nbt.copyTagWithoutId(), "Fuel", 0),
            0,
            ModuleManager.get()
                .get(FurnaceMinecartTweaks.class)
                .map(m -> Andromeda.MAIN.get(FurnaceMinecartTweaks.CONFIG).maxFuel)
                .orElse(32000));

        if (furnaceMinecart.fuel > 0) {
          furnaceMinecart.push =
              new Vec3(furnaceMinecart.getX() - pos.getX(), 0, furnaceMinecart.getZ() - pos.getZ());
        }
      }

      return furnaceMinecart;
    });
  }

  public interface PlaceBehavior {
    AbstractMinecart dispense(
        ItemStack stack, Level world, double d, double e, double f, double g, BlockPos pos);
  }
}
