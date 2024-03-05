package me.melontini.andromeda.common.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UvTexturedButtonWidget extends ButtonWidget {
    protected final Identifier texture;
    protected final int u;
    protected final int v;
    protected final int hoveredVOffset;
    protected final int textureWidth;
    protected final int textureHeight;

    public UvTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, ButtonWidget.PressAction pressAction) {
        this(x, y, width, height, u, v, height, texture, 256, 256, pressAction);
    }

    public UvTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, ButtonWidget.PressAction pressAction) {
        this(x, y, width, height, u, v, hoveredVOffset, texture, 256, 256, pressAction);
    }

    public UvTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction) {
        this(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, ScreenTexts.EMPTY);
    }

    public UvTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction, Text message) {
        super(x, y, width, height, message, pressAction, DEFAULT_NARRATION_SUPPLIER);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.u = u;
        this.v = v;
        this.hoveredVOffset = hoveredVOffset;
        this.texture = texture;
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int i = v;
        if (!this.isNarratable()) {
            i = v + hoveredVOffset * 2;
        } else if (this.isSelected()) {
            i = v + hoveredVOffset;
        }

        RenderSystem.enableDepthTest();
        context.drawTexture(texture, this.getX(), this.getY(), (float)u, (float)i, width, height, textureWidth, textureHeight);
    }
}
