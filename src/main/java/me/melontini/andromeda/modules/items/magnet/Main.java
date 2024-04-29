package me.melontini.andromeda.modules.items.magnet;

import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.items.magnet.items.MagnetItem;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroups;

import static me.melontini.andromeda.common.Andromeda.id;

public class Main {

    public static final Keeper<MagnetItem> MAGNET = Keeper.create();

    Main(Magnet module) {
        MAGNET.init(RegistryUtil.register(CommonRegistries.items(), id("magnet"), () -> new MagnetItem(new FabricItemSettings().maxCount(1))));

        AndromedaItemGroup.accept(a -> a.keeper(module, ItemGroups.TOOLS, MAGNET));
    }
}
