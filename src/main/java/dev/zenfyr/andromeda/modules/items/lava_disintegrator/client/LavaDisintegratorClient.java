package dev.zenfyr.andromeda.modules.items.lava_disintegrator.client;

import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class LavaDisintegratorClient {

  public static void spawnLavaParticles(int count) {
    var client = Minecraft.getInstance();
    int x = (int) (client.mouseHandler.xpos()
        * (double) client.getWindow().getGuiScaledWidth()
        / (double) client.getWindow().getScreenWidth());
    int y = (int) (client.mouseHandler.ypos()
        * (double) client.getWindow().getGuiScaledHeight()
        / (double) client.getWindow().getScreenHeight());
    for (int i = 0; i < count; i++) {
      ScreenParticles.get(client)
          .addParticle(
              client.gui.screen(), VanillaParticles.create(ParticleTypes.LAVA, x, y, 0.0, 0.0));
    }

    client.level.playSound(
        client.player,
        client.player,
        SoundEvents.LAVA_EXTINGUISH,
        SoundSource.AMBIENT,
        0.8f,
        0.8F + MathUtil.threadRandom().nextFloat() * 0.4F);
  }
}
