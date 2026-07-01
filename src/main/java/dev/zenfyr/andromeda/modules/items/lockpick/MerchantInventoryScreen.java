package dev.zenfyr.andromeda.modules.items.lockpick;

import dev.zenfyr.andromeda.common.Andromeda;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MerchantInventoryScreen
    extends AbstractContainerScreen<MerchantInventoryScreenHandler> {

  private static final Identifier TEXTURE = Andromeda.id("textures/gui/merchant_inventory.png");

  public MerchantInventoryScreen(
      MerchantInventoryScreenHandler handler, Inventory inventory, Component title) {
    super(handler, inventory, title);
  }

  @Override
  public void extractRenderState(
      GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    this.extractBackground(context, mouseX, mouseY, delta);
    super.extractRenderState(context, mouseX, mouseY, delta);
    this.extractTooltip(context, mouseX, mouseY);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
    context.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 42107532, false);
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float a) {
    int i = (this.width - this.imageWidth) / 2;
    int j = (this.height - this.imageHeight) / 2;
    context.blit(
        RenderPipelines.GUI_TEXTURED,
        TEXTURE,
        i,
        j,
        0,
        0,
        this.imageWidth,
        this.imageHeight,
        256,
        256);
  }

  public static void onClient() {
    if (MerchantInventoryScreenHandler.INSTANCE.isPresent()) {
      MenuScreens.register(
          MerchantInventoryScreenHandler.INSTANCE.orThrow(), MerchantInventoryScreen::new);
    }
  }
}
