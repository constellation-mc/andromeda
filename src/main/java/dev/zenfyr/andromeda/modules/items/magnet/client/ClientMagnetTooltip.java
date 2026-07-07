package dev.zenfyr.andromeda.modules.items.magnet.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.world.item.component.BundleContents;

public class ClientMagnetTooltip extends ClientBundleTooltip {

  public static final ScopedValue<Boolean> IS_MAGNET = ScopedValue.newInstance();

  public ClientMagnetTooltip(BundleContents contents) {
    super(contents);
  }

  @Override
  public int getHeight(Font font) {
    return super.getHeight(font) - 13 - 8;
  }

  @Override
  public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
    ScopedValue.where(IS_MAGNET, Boolean.TRUE)
        .run(() -> super.extractImage(font, x, y, w, h, graphics));
  }
}
