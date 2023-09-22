package me.melontini.andromeda.registries;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.screens.FletchingScreenHandler;
import me.melontini.andromeda.screens.MerchantInventoryScreenHandler;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.minecraft.screen.ScreenHandlerType;

import java.util.Objects;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.share;

public class ScreenHandlerRegistry {

    private static ScreenHandlerRegistry INSTANCE;

    public final ScreenHandlerType<FletchingScreenHandler> FLETCHING_SCREEN_HANDLER = RegistryUtil.createScreenHandler(Config.get().usefulFletching, id("fletching"), () -> FletchingScreenHandler::new);
    public final ScreenHandlerType<MerchantInventoryScreenHandler> MERCHANT_INVENTORY_SCREEN_HANDLER = RegistryUtil.createScreenHandler(id("merchant_inventory"), () -> MerchantInventoryScreenHandler::new);

    public static ScreenHandlerRegistry get() {
        return Objects.requireNonNull(INSTANCE, "%s requested too early!".formatted(INSTANCE.getClass()));
    }

    public static void register() {
        if (INSTANCE != null) throw new IllegalStateException("%s already initialized!".formatted(INSTANCE.getClass()));

        INSTANCE = new ScreenHandlerRegistry();
        share("andromeda:screen_handler_registry", INSTANCE);
        AndromedaLog.info("%s init complete!".formatted(INSTANCE.getClass().getSimpleName()));
    }
}
