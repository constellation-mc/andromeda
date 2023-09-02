package me.melontini.andromeda.registries;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.screens.FletchingScreenHandler;
import me.melontini.andromeda.screens.MerchantInventoryScreenHandler;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.util.SharedConstants.MODID;

public class ScreenHandlerRegistry {

    public static ScreenHandlerType<FletchingScreenHandler> FLETCHING_SCREEN_HANDLER;
    public static ScreenHandlerType<MerchantInventoryScreenHandler> MERCHANT_INVENTORY_SCREEN_HANDLER;

    public static void register() {
        FLETCHING_SCREEN_HANDLER = RegistryUtil.createScreenHandler(Andromeda.CONFIG.usefulFletching, new Identifier(MODID, "fletching"), () -> FletchingScreenHandler::new);

        MERCHANT_INVENTORY_SCREEN_HANDLER = RegistryUtil.createScreenHandler(new Identifier(MODID, "merchant_inventory"), () -> MerchantInventoryScreenHandler::new);

        AndromedaLog.info("ScreenHandlerRegistry init complete!");
    }
}
