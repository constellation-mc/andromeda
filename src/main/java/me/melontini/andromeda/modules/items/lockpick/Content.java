package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.screen.ScreenHandlerType;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.common.registries.Common.start;

public class Content {

    private static Lockpick MODULE;

    public static final Keeper<LockpickItem> LOCKPICK = start(() -> ContentBuilder.ItemBuilder
            .create(id("lockpick"), () -> new LockpickItem(new FabricItemSettings().maxCount(16)))
            .itemGroup(CommonItemGroups.tools()))
            .afterInit(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(ModuleManager.quick(Lockpick.class), item)));

    public static final Keeper<ScreenHandlerType<MerchantInventoryScreenHandler>> MERCHANT_INVENTORY = Keeper.of(() ->
            RegistryUtil.createScreenHandler(() -> MODULE.config().villagerInventory,
                    id("merchant_inventory"), () -> MerchantInventoryScreenHandler::new));
}
