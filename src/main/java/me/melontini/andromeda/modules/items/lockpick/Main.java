package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroups;

import static me.melontini.andromeda.common.Andromeda.id;

public class Main {

    Main(Lockpick module, Lockpick.Config config) {
        LockpickItem.INSTANCE.init(RegistryUtil.register(CommonRegistries.items(), id("lockpick"), () -> new LockpickItem(new FabricItemSettings().maxCount(16))));
        MerchantInventoryScreenHandler.INSTANCE.init(RegistryUtil.register(config.villagerInventory, CommonRegistries.screenHandlers(),
                id("merchant_inventory"), RegistryUtil.screenHandlerType(MerchantInventoryScreenHandler::new)));

        AndromedaItemGroup.accept(acceptor -> acceptor.keeper(module, ItemGroups.TOOLS, LockpickItem.INSTANCE));
    }
}
