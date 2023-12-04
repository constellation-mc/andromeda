package me.melontini.andromeda.modules.blocks.better_fletching_table;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.minecraft.screen.ScreenHandlerType;

import static me.melontini.andromeda.common.registries.Common.id;

public class Content {

    public static final Keeper<ScreenHandlerType<FletchingScreenHandler>> FLETCHING = Keeper.of(() -> () ->
            RegistryUtil.createScreenHandler(() -> ModuleManager.quick(BetterFletchingTable.class).config().enabled,
                    id("fletching"), () -> FletchingScreenHandler::new));
}
