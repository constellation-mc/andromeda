package me.melontini.andromeda.registries;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.screens.FletchingScreenHandler;
import me.melontini.andromeda.screens.MerchantInventoryScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.Andromeda.MODID;

public class ScreenHandlerRegistry {
    public static ScreenHandlerType<FletchingScreenHandler> FLETCHING_SCREEN_HANDLER;
    public static ScreenHandlerType<MerchantInventoryScreenHandler> MERCHANT_INVENTORY_SCREEN_HANDLER;
    public static void register() {
        if (Andromeda.CONFIG.usefulFletching) {
            FLETCHING_SCREEN_HANDLER = new ScreenHandlerType<>(FletchingScreenHandler::new);
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(MODID, "fletching"), FLETCHING_SCREEN_HANDLER);
        }

        MERCHANT_INVENTORY_SCREEN_HANDLER = new ScreenHandlerType<>(MerchantInventoryScreenHandler::new);
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(MODID, "merchant_inventory"), MERCHANT_INVENTORY_SCREEN_HANDLER);
    }
}
