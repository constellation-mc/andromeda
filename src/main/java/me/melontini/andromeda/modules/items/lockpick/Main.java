package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.screen.ScreenHandlerType;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {

    public static final Keeper<LockpickItem> LOCKPICK = Keeper.create();
    public static final Keeper<ScreenHandlerType<MerchantInventoryScreenHandler>> MERCHANT_INVENTORY = Keeper.create();

    Main(Lockpick module, Lockpick.Config config) {
        LOCKPICK.init(ContentBuilder.ItemBuilder
                .create(id("lockpick"), () -> new LockpickItem(new FabricItemSettings().maxCount(16)))
                .itemGroup(CommonItemGroups.tools()).build());
        MERCHANT_INVENTORY.init(RegistryUtil.createScreenHandler(config.villagerInventory,
                id("merchant_inventory"), () -> MerchantInventoryScreenHandler::new));

        AndromedaItemGroup.accept(acceptor -> acceptor.keeper(module, LOCKPICK));
    }
}
