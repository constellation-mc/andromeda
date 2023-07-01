package me.melontini.andromeda.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.andromeda.screens.FletchingScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static me.melontini.andromeda.Andromeda.MODID;

public class FletchingScreen extends ForgingScreen<FletchingScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(MODID, "textures/gui/fletching.png");

    public FletchingScreen(FletchingScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title, TEXTURE);
        this.titleX = 60;
        this.titleY = 18;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        RenderSystem.disableBlend();
        super.drawForeground(context, mouseX, mouseY);
    }

    @Override
    protected void drawInvalidRecipeArrow(DrawContext context, int x, int y) {
        if ((handler.getSlot(0).hasStack() && !handler.getSlot(1).hasStack()) || (!handler.getSlot(0).hasStack() && handler.getSlot(1).hasStack())) {
            context.drawTexture(TEXTURE, x + 65, y + 46, this.backgroundWidth, 0, 28, 21);
        }
    }
}
