package me.melontini.andromeda.modules.blocks.better_fletching_table;

import me.melontini.andromeda.registries.Keeper;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.minecraft.screen.ScreenHandlerType;

import static me.melontini.andromeda.registries.Common.id;

public class Content {
    @Feature("usefulFletching")
    public static final Keeper<ScreenHandlerType<FletchingScreenHandler>> FLETCHING = Keeper.of(() -> () ->
            RegistryUtil.createScreenHandler(id("fletching"), () -> FletchingScreenHandler::new));
}
