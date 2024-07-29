package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MerchantInventoryScreen extends HandledScreen<MerchantInventoryScreenHandler> {

  private static final Identifier TEXTURE = Andromeda.id("textures/gui/merchant_inventory.png");

  public MerchantInventoryScreen(
      MerchantInventoryScreenHandler handler, PlayerInventory inventory, Text title) {
    super(handler, inventory, title);
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    this.renderBackground(context, mouseX, mouseY, delta);
    super.render(context, mouseX, mouseY, delta);
    this.drawMouseoverTooltip(context, mouseX, mouseY);
  }

  @Override
  protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 42107532, false);
  }

  @Override
  protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    int i = (this.width - this.backgroundWidth) / 2;
    int j = (this.height - this.backgroundHeight) / 2;
    context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
  }

  public static void onClient() {
    MerchantInventoryScreenHandler.INSTANCE.ifPresent(
        s -> HandledScreens.register(s, MerchantInventoryScreen::new));
  }
}
