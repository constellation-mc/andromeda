package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.modules.entities.boats.items.AndromedaBoatItem;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.common.registries.Common.id;

public class BoatItems {

    public static void init(Boats module) {
        for (BoatEntity.Type value : BoatEntity.Type.values()) {
            ContentBuilder.ItemBuilder.create(boatId(value, "furnace"), () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_FURNACE, value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(CommonItemGroups.transport()).register(module.config().isFurnaceBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

            ContentBuilder.ItemBuilder.create(boatId(value, "jukebox"), () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_JUKEBOX, value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(CommonItemGroups.transport()).register(module.config().isJukeboxBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

            ContentBuilder.ItemBuilder.create(boatId(value, "tnt"), () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_TNT, value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(CommonItemGroups.transport()).register(module.config().isTNTBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

            ContentBuilder.ItemBuilder.create(boatId(value, "hopper"), () -> new AndromedaBoatItem<>(BoatEntities.BOAT_WITH_HOPPER, value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(CommonItemGroups.transport()).register(module.config().isHopperBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

        }
    }

    public static Identifier boatId(BoatEntity.Type type, String boat) {
        return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
    }
}
