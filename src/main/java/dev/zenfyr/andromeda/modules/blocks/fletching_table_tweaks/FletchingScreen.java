package dev.zenfyr.andromeda.modules.blocks.fletching_table_tweaks;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.pulsar.api.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class FletchingScreen extends ItemCombinerScreen<FletchingScreenHandler> {

  private static final ResourceLocation TEXTURE = Andromeda.id("textures/gui/fletching.png");

  public FletchingScreen(
      FletchingScreenHandler handler, Inventory playerInventory, Component title) {
    super(handler, playerInventory, title, TEXTURE);
    this.titleLabelX = 60;
    this.titleLabelY = 18;
  }

  @Override
  protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
    super.renderLabels(context, mouseX, mouseY);
  }

  @Override
  protected void renderErrorIcon(GuiGraphics context, int x, int y) {
    if (menu.getSlot(0).hasItem()
        && menu.getSlot(1).hasItem()
        && !menu.getSlot(2).hasItem()) {
      context.blit(
          RenderPipelines.GUI_TEXTURED,
          TEXTURE,
          x + 99,
          y + 45,
          this.imageWidth,
          0,
          28,
          21,
          256,
          256);
    }
  }

  public static void onClient() {
    if (FletchingScreenHandler.FLETCHING.isPresent()) {
      MenuScreens.register(FletchingScreenHandler.FLETCHING.get(), FletchingScreen::new);
    }

    ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
      int i = stack.getOrDefault(FletchingScreenHandler.TIGHTENED.get(), 0);
      if (i > 0)
        lines.add(
            TextUtil.translatable("tooltip.andromeda.bow.tight", i).withStyle(ChatFormatting.GRAY));
    });
  }
}
