package dev.zenfyr.andromeda.modules.misc.creative_mode_tab.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.CreativeModeTabMain;
import dev.zenfyr.pulsar.api.client.creativetab.CreativeModeTabAnimation;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

public class CreativeModeTabClient {

  private static final ResourceLocation BACKGROUND_TEXTURE =
      Andromeda.id("textures/gui/background.png");
  private static final ResourceLocation GALAXY_TEXTURE = Andromeda.id("textures/gui/galaxy.png");

  public static void init() {
    if (CreativeModeTabMain.TAB.isPresent()) {
      CreativeModeTabAnimation.setIconAnimation(
          CreativeModeTabMain.TAB.orThrow(), (tab, graphics, x, y, selected, isTopRow) -> {
            var pose = graphics.pose();
            RenderSystem.enableBlend();
            pose.pushPose();
            pose.translate(x + 8, y + 8, 0);
            graphics.blit(BACKGROUND_TEXTURE, -8, -8, 0f, 0f, 16, 16, 16, 16);
            pose.mulPose(Axis.ZN.rotationDegrees(Util.getMillis() * 0.05f));
            graphics.blit(GALAXY_TEXTURE, -8, -8, 0f, 0f, 16, 16, 16, 16);
            pose.popPose();
            RenderSystem.disableBlend();
          });
    }
  }
}
