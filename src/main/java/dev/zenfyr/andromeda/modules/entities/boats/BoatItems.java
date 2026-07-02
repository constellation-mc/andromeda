package dev.zenfyr.andromeda.modules.entities.boats;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.boats.items.AndromedaBoatItem;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.AndromedaCreativeTab;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class BoatItems {

  public static void init() {
    var module = ModuleManager.get().get(Boats.class).orElseThrow();
    var config = Andromeda.MAIN.get(Boats.MAIN_CONFIG);

    Map<BoatTypes.BoatVariant, BooleanSupplier> map = Map.of(
        BoatTypes.FURNACE, () -> config.isFurnaceBoatOn,
        BoatTypes.JUKEBOX, () -> config.isJukeboxBoatOn,
        BoatTypes.TNT, () -> config.isTNTBoatOn,
        BoatTypes.HOPPER, () -> config.isFurnaceBoatOn);

    List<Item> list = new ArrayList<>();
    for (BoatTypes.BoatType type : BoatTypes.getBoatTypes()) {
      map.forEach((variant, conf) -> {
        var itemKey = BoatTypes.key(Registries.ITEM, type, variant);
        var entityKey = BoatTypes.key(Registries.ENTITY_TYPE, type, variant);

        if (conf.getAsBoolean()) {
          list.add(Registry.register(
              BuiltInRegistries.ITEM,
              itemKey,
              new AndromedaBoatItem<>(
                  () -> BuiltInRegistries.ENTITY_TYPE.getValue(entityKey),
                  new Item.Properties().setId(itemKey).stacksTo(1))));
        }
      });
    }

    AndromedaCreativeTab.BUS.listen(
        acceptor -> acceptor.items(module, CreativeModeTabs.TOOLS_AND_UTILITIES, list));
  }
}
