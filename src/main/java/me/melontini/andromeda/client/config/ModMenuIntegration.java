package me.melontini.andromeda.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.andromeda.config.AndromedaConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
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
                    addDrawableChild(screen, new TexturedButtonWidget(screen.width - 40, 13, 20, 20, 0, 0, 20, AndromedaClient.get().WIKI_BUTTON_TEXTURE, 32, 64, button -> screen.handleTextClick(AndromedaClient.get().WIKI_LINK)));
                }
            });
            return c;
        };
    }

    private static <T extends Element & Drawable & Selectable> T addDrawableChild(Screen screen, T drawableElement) {
        screen.drawables.add(drawableElement);
        screen.children.add(drawableElement);
        screen.selectables.add(drawableElement);
        return drawableElement;
    }
}