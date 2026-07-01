package dev.zenfyr.andromeda.modules.items.magnet.client;

import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class MagnetClient {

  public static void upgradeParticles(Player player) {
    if (player.level.isClientSide()) {
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
              VanillaParticles.create(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 7));
    }
  }

  public static void itemParticles(ItemStack stack, Player player) {
    if (player.level.isClientSide()) {
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
              VanillaParticles.create(
                  new ItemParticleOption(
                      ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)),
                  x,
                  y,
                  0.5,
                  0.5,
                  0.1,
                  7));
    }
  }
}
