package dev.zenfyr.andromeda.modules.items.magnet.client;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.gui.gui_particles.GuiParticles;
import dev.zenfyr.andromeda.modules.items.magnet.MagnetTooltip;
import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;

public class MagnetClient {

  public static void init() {
    ClientTooltipComponentCallback.EVENT.register(component -> {
      if (component instanceof MagnetTooltip(BundleContents contents))
        return new ClientMagnetTooltip(contents);
      return null;
    });
  }

  public static boolean hideParticles() {
    return ModuleManager.get()
        .get(GuiParticles.class)
        .map(_ -> !AndromedaClient.CLIENT.get(GuiParticles.CONFIG).magnetParticles)
        .orElse(true);
  }

  public static void upgradeParticles(Player player) {
    if (hideParticles()) return;

    var client = Minecraft.getInstance();
    int x = (int) (client.mouseHandler.xpos()
        * (double) client.getWindow().getGuiScaledWidth()
        / (double) client.getWindow().getScreenWidth());
    int y = (int) (client.mouseHandler.ypos()
        * (double) client.getWindow().getGuiScaledHeight()
        / (double) client.getWindow().getScreenHeight());
    ScreenParticles.get(client)
        .addParticles(
            client.screen, VanillaParticles.create(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 7));
  }

  public static void itemParticles(ItemStack stack, Player player) {
    if (hideParticles()) return;

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
