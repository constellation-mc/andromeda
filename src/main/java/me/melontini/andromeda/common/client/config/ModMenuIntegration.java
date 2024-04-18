package me.melontini.andromeda.common.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import lombok.CustomLog;
import me.melontini.dark_matter.api.base.util.Support;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static me.melontini.andromeda.util.CommonValues.MODID;

@CustomLog
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    public static final Function<Screen, Screen> SCREEN_PROVIDER = Support.support("cloth-config", () -> AutoConfigScreen::get, () -> screen -> null);

    public static final Identifier WIKI_BUTTON_TEXTURE = new Identifier(MODID, "textures/gui/wiki_button.png");
    public static final Identifier LAB_BUTTON_TEXTURE = new Identifier(MODID, "textures/gui/lab_button.png");
    public static final Style WIKI_LINK = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://andromeda-wiki.pages.dev/"));

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SCREEN_PROVIDER::apply;
    }

    static <T extends Element & Drawable & Selectable> T addDrawableChild(Screen screen, T drawableElement) {
        screen.drawables.add(drawableElement);
        screen.children.add(drawableElement);
        screen.selectables.add(drawableElement);
        return drawableElement;
    }
}
