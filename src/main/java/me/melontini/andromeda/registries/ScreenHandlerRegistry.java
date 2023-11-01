package me.melontini.andromeda.registries;

import me.melontini.andromeda.screens.FletchingScreenHandler;
import me.melontini.andromeda.screens.MerchantInventoryScreenHandler;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

import java.util.Objects;

import static me.melontini.andromeda.registries.Common.*;

public class ScreenHandlerRegistry {

    private static ScreenHandlerRegistry INSTANCE;

    @Feature("usefulFletching")
    public final Keeper<ScreenHandlerType<FletchingScreenHandler>> FLETCHING = Keeper.of(() -> () -> RegistryUtil.createScreenHandler(id("fletching"), () -> FletchingScreenHandler::new));

    @Feature("lockpick.villagerInventory")
    public final Keeper<ScreenHandlerType<MerchantInventoryScreenHandler>> MERCHANT_INVENTORY = Keeper.of(() -> () -> RegistryUtil.createScreenHandler(id("merchant_inventory"), () -> MerchantInventoryScreenHandler::new));

    public static ScreenHandlerRegistry get() {
        return Objects.requireNonNull(INSTANCE, "%s requested too early!".formatted(INSTANCE.getClass()));
    }

    static void register() {
        if (INSTANCE != null) throw new IllegalStateException("%s already initialized!".formatted(INSTANCE.getClass()));

        INSTANCE = new ScreenHandlerRegistry();
        bootstrap(INSTANCE);

        share("andromeda:screen_handler_registry", INSTANCE);
        AndromedaLog.info("%s init complete!".formatted(INSTANCE.getClass().getSimpleName()));
    }
}
