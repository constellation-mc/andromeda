package dev.zenfyr.andromeda.modules.misc.unknown.client;

import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

public class UnknownClient {

  public static void onClient() {}

  public static void roseParticles() {
    var client = Minecraft.getInstance();
    int x = (int) (client.mouseHandler.xpos()
        * (double) client.getWindow().getGuiScaledWidth()
        / (double) client.getWindow().getScreenWidth());
    int y = (int) (client.mouseHandler.ypos()
        * (double) client.getWindow().getGuiScaledHeight()
        / (double) client.getWindow().getScreenHeight());
    ScreenParticles.get(client)
        .addParticles(
            client.screen,
            VanillaParticles.create(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.08, 10));
  }
}
