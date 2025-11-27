package dev.zenfyr.andromeda.modules.gui.gui_particles;

import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.pulsar.client.particles.ScreenParticleHelper;
import dev.zenfyr.pulsar.util.MathUtil;
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
          if (furnaceScreen.getMenu().isLit() && MathUtil.threadRandom().nextInt(10) == 0) {
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
