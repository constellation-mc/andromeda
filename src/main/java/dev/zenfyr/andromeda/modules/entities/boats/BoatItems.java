package dev.zenfyr.andromeda.modules.entities.boats;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.AndromedaItemGroup;
import dev.zenfyr.andromeda.modules.entities.boats.items.AndromedaBoatItem;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class BoatItems {

  public static void init() {
    var module = ModuleManager.get().get(Boats.class).orElseThrow();
    var config = Andromeda.MAIN.get(Boats.MAIN_CONFIG);

    List<Item> list = new ArrayList<>();
    for (Boat.Type value : Boat.Type.values()) {
      Optional.ofNullable(RegistryUtil.register(
              config.isFurnaceBoatOn,
              BuiltInRegistries.ITEM,
              boatId(value, "furnace"),
              () -> new AndromedaBoatItem<>(
                  BoatEntities.BOAT_WITH_FURNACE, value, new FabricItemSettings().stacksTo(1))))
          .ifPresent(list::add);

      Optional.ofNullable(RegistryUtil.register(
              config.isJukeboxBoatOn,
              BuiltInRegistries.ITEM,
              boatId(value, "jukebox"),
              () -> new AndromedaBoatItem<>(
                  BoatEntities.BOAT_WITH_JUKEBOX, value, new FabricItemSettings().stacksTo(1))))
          .ifPresent(list::add);

      Optional.ofNullable(RegistryUtil.register(
              config.isTNTBoatOn,
              BuiltInRegistries.ITEM,
              boatId(value, "tnt"),
              () -> new AndromedaBoatItem<>(
                  BoatEntities.BOAT_WITH_TNT, value, new FabricItemSettings().stacksTo(1))))
          .ifPresent(list::add);

      Optional.ofNullable(RegistryUtil.register(
              config.isHopperBoatOn,
              BuiltInRegistries.ITEM,
              boatId(value, "hopper"),
              () -> new AndromedaBoatItem<>(
                  BoatEntities.BOAT_WITH_HOPPER, value, new FabricItemSettings().stacksTo(1))))
          .ifPresent(list::add);
    }
    AndromedaItemGroup.BUS.listen(
        acceptor -> acceptor.items(module, CreativeModeTabs.TOOLS_AND_UTILITIES, list));
  }

  public static ResourceLocation boatId(Boat.Type type, String boat) {
    return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
  }
}
