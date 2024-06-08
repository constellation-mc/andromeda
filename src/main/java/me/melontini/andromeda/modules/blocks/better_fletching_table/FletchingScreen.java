package me.melontini.andromeda.modules.blocks.better_fletching_table;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;


@Environment(EnvType.CLIENT)
public class FletchingScreen extends ForgingScreen<FletchingScreenHandler> {

    private static final Identifier TEXTURE = Andromeda.id("textures/gui/fletching.png");

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
        if (handler.getSlot(0).hasStack() && handler.getSlot(1).hasStack() && !handler.getSlot(2).hasStack()) {
            context.drawTexture(TEXTURE, x + 99, y + 45, this.backgroundWidth, 0, 28, 21);
        }
    }

    public static void onClient() {
        FletchingScreenHandler.FLETCHING.ifPresent(s -> HandledScreens.register(s, FletchingScreen::new));

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null) return;

            int i = nbt.getInt("AM-Tightened");
            if (i > 0) lines.add(TextUtil.translatable("tooltip.andromeda.bow.tight", i).formatted(Formatting.GRAY));
        });
    }
}
