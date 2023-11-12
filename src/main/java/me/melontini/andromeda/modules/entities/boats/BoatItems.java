package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.modules.entities.boats.items.FurnaceBoatItem;
import me.melontini.andromeda.modules.entities.boats.items.HopperBoatItem;
import me.melontini.andromeda.modules.entities.boats.items.JukeboxBoatItem;
import me.melontini.andromeda.modules.entities.boats.items.TNTBoatItem;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.registries.Common.id;

public class BoatItems {

    public static void init() {
        for (BoatEntity.Type value : BoatEntity.Type.values()) {
            ContentBuilder.ItemBuilder.create(boatId(value, "furnace"), () -> new FurnaceBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(Config.get().newBoats.isFurnaceBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "jukebox"), () -> new JukeboxBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(Config.get().newBoats.isJukeboxBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "tnt"), () -> new TNTBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(Config.get().newBoats.isTNTBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "hopper"), () -> new HopperBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(Config.get().newBoats.isHopperBoatOn).build();
        }
    }

    public static Identifier boatId(BoatEntity.Type type, String boat) {
        return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
    }
}
