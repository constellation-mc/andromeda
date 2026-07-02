package dev.zenfyr.andromeda.modules.misc.creative_mode_tab.client;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.CreativeModeTabMain;
import dev.zenfyr.pulsar.api.client.creativetab.CreativeModeTabAnimation;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class CreativeModeTabClient {

  private static final Identifier BACKGROUND_TEXTURE = Andromeda.id("textures/gui/background.png");
  private static final Identifier GALAXY_TEXTURE = Andromeda.id("textures/gui/galaxy.png");

  public static void init() {
    if (CreativeModeTabMain.TAB.isPresent()) {
      CreativeModeTabAnimation.setIconAnimation(
          CreativeModeTabMain.TAB.orThrow(), (tab, graphics, x, y, selected, isTopRow) -> {
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x + 8, y + 8);
            graphics.blit(
                RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, -8, -8, 0f, 0f, 16, 16, 16, 16);
            pose.rotate(Util.getMillis() * 0.0005f);
            graphics.blit(
                RenderPipelines.GUI_TEXTURED, GALAXY_TEXTURE, -8, -8, 0f, 0f, 16, 16, 16, 16);
            pose.popMatrix();
          });
    }
  }
}
