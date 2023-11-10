package me.melontini.andromeda.registries;

import me.melontini.andromeda.screens.FletchingScreenHandler;
import me.melontini.andromeda.screens.MerchantInventoryScreenHandler;
import me.melontini.andromeda.util.annotations.AndromedaRegistry;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.minecraft.screen.ScreenHandlerType;

import static me.melontini.andromeda.registries.Common.id;

@AndromedaRegistry("andromeda:screen_handler_registry")
public class ScreenHandlerRegistry {

    @Feature("usefulFletching")
    public static final Keeper<ScreenHandlerType<FletchingScreenHandler>> FLETCHING = Keeper.of(() -> () ->
            RegistryUtil.createScreenHandler(id("fletching"), () -> FletchingScreenHandler::new));

    @Feature("lockpick.villagerInventory")
    public static final Keeper<ScreenHandlerType<MerchantInventoryScreenHandler>> MERCHANT_INVENTORY = Keeper.of(() -> () ->
            RegistryUtil.createScreenHandler(id("merchant_inventory"), () -> MerchantInventoryScreenHandler::new));
}
