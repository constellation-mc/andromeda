package dev.zenfyr.andromeda.modules.entities.boats;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.boats.items.AndromedaBoatItem;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.AndromedaCreativeTab;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
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
      if (config.isFurnaceBoatOn) {
        list.add(Registry.register(
            BuiltInRegistries.ITEM,
            boatId(value, "furnace"),
            new AndromedaBoatItem<>(
                BoatEntities.BOAT_WITH_FURNACE, value, new FabricItemSettings().stacksTo(1))));
      }

      if (config.isJukeboxBoatOn) {
        list.add(Registry.register(
            BuiltInRegistries.ITEM,
            boatId(value, "jukebox"),
            new AndromedaBoatItem<>(
                BoatEntities.BOAT_WITH_JUKEBOX, value, new FabricItemSettings().stacksTo(1))));
      }

      if (config.isTNTBoatOn) {
        list.add(Registry.register(
            BuiltInRegistries.ITEM,
            boatId(value, "tnt"),
            new AndromedaBoatItem<>(
                BoatEntities.BOAT_WITH_TNT, value, new FabricItemSettings().stacksTo(1))));
      }

      if (config.isHopperBoatOn) {
        list.add(Registry.register(
            BuiltInRegistries.ITEM,
            boatId(value, "hopper"),
            new AndromedaBoatItem<>(
                BoatEntities.BOAT_WITH_HOPPER, value, new FabricItemSettings().stacksTo(1))));
      }
    }
    AndromedaCreativeTab.BUS.listen(
        acceptor -> acceptor.items(module, CreativeModeTabs.TOOLS_AND_UTILITIES, list));
  }

  public static ResourceLocation boatId(Boat.Type type, String boat) {
    return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
  }
}
