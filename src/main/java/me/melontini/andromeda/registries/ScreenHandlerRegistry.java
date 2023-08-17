package me.melontini.andromeda.registries;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.screens.FletchingScreenHandler;
import me.melontini.andromeda.screens.MerchantInventoryScreenHandler;
import me.melontini.andromeda.util.AndromedaLog;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static me.melontini.andromeda.util.SharedConstants.MODID;

public class ScreenHandlerRegistry {
    public static ScreenHandlerType<FletchingScreenHandler> FLETCHING_SCREEN_HANDLER;
    public static ScreenHandlerType<MerchantInventoryScreenHandler> MERCHANT_INVENTORY_SCREEN_HANDLER;
    public static void register() {
        if (Andromeda.CONFIG.usefulFletching) {
            FLETCHING_SCREEN_HANDLER = new ScreenHandlerType<>(FletchingScreenHandler::new);
            Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "fletching"), FLETCHING_SCREEN_HANDLER);
        }

        MERCHANT_INVENTORY_SCREEN_HANDLER = new ScreenHandlerType<>(MerchantInventoryScreenHandler::new);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "merchant_inventory"), MERCHANT_INVENTORY_SCREEN_HANDLER);

        AndromedaLog.info("ScreenHandlerRegistry init complete!");
    }
}
