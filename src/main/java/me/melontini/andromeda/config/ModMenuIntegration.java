package me.melontini.andromeda.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.andromeda.mixin.accessors.ScreenAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Screen c = AutoConfig.getConfigScreen(AndromedaConfig.class, parent).get();
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen == c) {
                    ((ScreenAccessor) screen).andromeda$addDrawableChild(new TexturedButtonWidget(screen.width - 40, 13, 20, 20, 0, 0, 20, AndromedaClient.WIKI_BUTTON_TEXTURE, 32, 64, button -> screen.handleTextClick(AndromedaClient.WIKI_LINK)));
                }
            });
            return c;
        };
    }
}
