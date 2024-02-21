package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {

    Main(Lockpick module, Lockpick.Config config) {
        LockpickItem.INSTANCE.init(ContentBuilder.ItemBuilder
                .create(id("lockpick"), () -> new LockpickItem(new FabricItemSettings().maxCount(16)))
                .itemGroup(CommonItemGroups.tools()).build());
        MerchantInventoryScreenHandler.INSTANCE.init(RegistryUtil.createScreenHandler(config.villagerInventory,
                id("merchant_inventory"), () -> MerchantInventoryScreenHandler::new));

        AndromedaItemGroup.accept(acceptor -> acceptor.keeper(module, LockpickItem.INSTANCE));
    }
}
