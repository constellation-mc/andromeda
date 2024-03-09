package me.melontini.andromeda.common.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import lombok.CustomLog;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.util.CommonValues.MODID;

@CustomLog
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    public static final Identifier WIKI_BUTTON_TEXTURE = new Identifier(MODID, "textures/gui/wiki_button.png");
    public static final Identifier LAB_BUTTON_TEXTURE = new Identifier(MODID, "textures/gui/lab_button.png");
    public static final Style WIKI_LINK = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://andromeda-wiki.pages.dev/"));

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> Support.get("cloth-config", () -> () -> {
            Screen screen = AutoConfigScreen.get(parent);
            ScreenEvents.AFTER_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
                if (screen == screen1) {
                    var wiki = new TexturedButtonWidget(screen.width - 40, 13, 20, 20, 0, 0, 20, WIKI_BUTTON_TEXTURE, 32, 64, button -> {
                        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
                            Debug.load();
                            Support.runWeak(EnvType.CLIENT, () -> () -> ScreenParticleHelper.addScreenParticles(
                                    ParticleTypes.ANGRY_VILLAGER, screen.width - 30, 23, 0.5, 0.5, 0.5, 1
                            ));
                            LOGGER.info("Reloaded Debug Keys!");
                        } else {
                            screen.handleTextClick(WIKI_LINK);
                        }
                    });
                    //wiki.setTooltip(Tooltip.of(TextUtil.translatable("config.andromeda.button.wiki")));
                    addDrawableChild(screen, wiki);

                    var lab = new TexturedButtonWidget(screen.width - 62, 13, 20, 20, 0, 0, 20, LAB_BUTTON_TEXTURE, 32, 64, button -> client.setScreen(AutoConfigScreen.getLabScreen(screen1)));
                    //lab.setTooltip(Tooltip.of(TextUtil.translatable("config.andromeda.button.lab.tooltip")));
                    addDrawableChild(screen, lab);
                }
            });
            return screen;
        }).orElse(null);
    }

    private static <T extends Element & Drawable & Selectable> T addDrawableChild(Screen screen, T drawableElement) {
        screen.drawables.add(drawableElement);
        screen.children.add(drawableElement);
        screen.selectables.add(drawableElement);
        return drawableElement;
    }
}
