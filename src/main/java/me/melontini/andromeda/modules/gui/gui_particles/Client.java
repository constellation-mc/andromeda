package me.melontini.andromeda.modules.gui.gui_particles;

import me.melontini.andromeda.common.AndromedaClient;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.core.particles.ParticleTypes;

public class Client {

  static void init() {
    var config = AndromedaClient.CLIENT.get(GuiParticles.CONFIG);

    ScreenEvents.BEFORE_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
      if (screen1 instanceof AbstractFurnaceScreen<?> abstractFurnaceScreen
          && config.furnaceScreenParticles) {
        ScreenEvents.afterTick(abstractFurnaceScreen).register(screen -> {
          AbstractFurnaceScreen<?> furnaceScreen = (AbstractFurnaceScreen<?>) screen;
          if (furnaceScreen.getMenu().isLit()
              && MathUtil.threadRandom().nextInt(10) == 0) {
            ScreenParticleHelper.addScreenParticle(
                screen,
                ParticleTypes.FLAME,
                MathUtil.nextDouble(furnaceScreen.leftPos + 56, furnaceScreen.leftPos + 56 + 14),
                furnaceScreen.topPos + 36 + 13,
                MathUtil.nextDouble(-0.01, 0.01),
                0.05);
          }
        });
      }
    });
  }
}
