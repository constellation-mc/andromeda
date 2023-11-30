package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.boats.items.FurnaceBoatItem;
import me.melontini.andromeda.modules.entities.boats.items.HopperBoatItem;
import me.melontini.andromeda.modules.entities.boats.items.JukeboxBoatItem;
import me.melontini.andromeda.modules.entities.boats.items.TNTBoatItem;
import me.melontini.andromeda.registries.AndromedaItemGroup;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.registries.Common.id;

public class BoatItems {

    public static void init() {
        Boats module = ModuleManager.quick(Boats.class);

        for (BoatEntity.Type value : BoatEntity.Type.values()) {
            ContentBuilder.ItemBuilder.create(boatId(value, "furnace"), () -> new FurnaceBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(Registries.ITEM_GROUP.get(ItemGroups.TOOLS)).register(module.config().isFurnaceBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

            ContentBuilder.ItemBuilder.create(boatId(value, "jukebox"), () -> new JukeboxBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(Registries.ITEM_GROUP.get(ItemGroups.TOOLS)).register(module.config().isJukeboxBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

            ContentBuilder.ItemBuilder.create(boatId(value, "tnt"), () -> new TNTBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(Registries.ITEM_GROUP.get(ItemGroups.TOOLS)).register(module.config().isTNTBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

            ContentBuilder.ItemBuilder.create(boatId(value, "hopper"), () -> new HopperBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(Registries.ITEM_GROUP.get(ItemGroups.TOOLS)).register(module.config().isHopperBoatOn).optional()
                    .ifPresent(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(module, item)));

        }
    }

    public static Identifier boatId(BoatEntity.Type type, String boat) {
        return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
    }
}
