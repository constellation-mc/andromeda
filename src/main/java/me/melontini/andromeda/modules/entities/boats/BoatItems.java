package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.modules.entities.boats.items.AndromedaBoatItem;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.melontini.andromeda.common.Andromeda.id;

public class BoatItems {

    public static void init(Boats module, Boats.Config config) {
        List<Item> list = new ArrayList<>();
        for (BoatEntity.Type value : BoatEntity.Type.values()) {
            Optional.ofNullable(RegistryUtil.register(config.isFurnaceBoatOn, Registries.ITEM, boatId(value, "furnace"),
                    () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_FURNACE, value, new FabricItemSettings().maxCount(1)))).ifPresent(list::add);

            Optional.ofNullable(RegistryUtil.register(config.isJukeboxBoatOn, Registries.ITEM, boatId(value, "jukebox"),
                    () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_JUKEBOX, value, new FabricItemSettings().maxCount(1)))).ifPresent(list::add);

            Optional.ofNullable(RegistryUtil.register(config.isTNTBoatOn, Registries.ITEM, boatId(value, "tnt"),
                    () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_TNT, value, new FabricItemSettings().maxCount(1)))).ifPresent(list::add);

            Optional.ofNullable(RegistryUtil.register(config.isHopperBoatOn, Registries.ITEM, boatId(value, "hopper"),
                    () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_HOPPER, value, new FabricItemSettings().maxCount(1)))).ifPresent(list::add);
        }
        AndromedaItemGroup.accept(acceptor -> acceptor.items(module, ItemGroups.TOOLS, list));
    }

    public static Identifier boatId(BoatEntity.Type type, String boat) {
        return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
    }
}
