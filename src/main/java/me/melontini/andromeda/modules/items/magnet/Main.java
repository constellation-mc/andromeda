package me.melontini.andromeda.modules.items.magnet;

import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.modules.items.magnet.items.MagnetItem;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {

    public static final Keeper<MagnetItem> MAGNET = Keeper.create();

    Main(Magnet module) {
        MAGNET.init(ContentBuilder.ItemBuilder.create(id("magnet"), () -> new MagnetItem(new FabricItemSettings().maxCount(1)))
                .itemGroup(CommonItemGroups.tools()).build());

        AndromedaItemGroup.accept(a -> a.keeper(module, MAGNET));
    }
}
