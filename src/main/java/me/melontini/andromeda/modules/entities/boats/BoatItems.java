package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.boats.items.*;
import me.melontini.dark_matter.api.base.util.classes.Lazy;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.registries.Common.id;

public class BoatItems {
    private static final Lazy<Boats> module = Lazy.of(() -> () -> ModuleManager.quick(Boats.class));

    public static void init() {
        for (BoatEntity.Type value : BoatEntity.Type.values()) {
            ContentBuilder.ItemBuilder.create(boatId(value, "furnace"), () -> new FurnaceBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(module.get().config().isFurnaceBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "jukebox"), () -> new JukeboxBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(module.get().config().isJukeboxBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "tnt"), () -> new TNTBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(module.get().config().isTNTBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "hopper"), () -> new HopperBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(module.get().config().isHopperBoatOn).build();
            ContentBuilder.ItemBuilder.create(boatId(value, "chest"), () -> new ChestBoatItem(value, new FabricItemSettings().maxCount(1)))
                    .itemGroup(ItemGroup.TRANSPORTATION).register(module.get().config().isChestBoatOn).build();
        }
    }

    public static Identifier boatId(BoatEntity.Type type, String boat) {
        return id(type.getName().replace(":", "_") + "_boat_with_" + boat);
    }
}
