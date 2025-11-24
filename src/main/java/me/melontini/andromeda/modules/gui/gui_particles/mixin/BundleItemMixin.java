package me.melontini.andromeda.modules.gui.gui_particles.mixin;

import me.melontini.andromeda.common.AndromedaClient;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
abstract class BundleItemMixin {

  @Inject(at = @At("RETURN"), method = "add")
  private static void andromeda$spawnParticles(
      ItemStack bundle, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
    if (cir.getReturnValueI() > 0
        && AndromedaClient.CLIENT.get(GuiParticles.CONFIG).bundleInputParticles) {
      var client = Minecraft.getInstance();
      if (client.isSameThread() && client.screen != null) {
        int x = (int) (client.mouseHandler.xpos()
            * (double) client.getWindow().getGuiScaledWidth()
            / (double) client.getWindow().getScreenWidth());
        int y = (int) (client.mouseHandler.ypos()
            * (double) client.getWindow().getGuiScaledHeight()
            / (double) client.getWindow().getScreenHeight());
        ScreenParticleHelper.addScreenParticles(
            new ItemParticleOption(ParticleTypes.ITEM, stack), x, y, 0.5, 0.5, 0.1, 7);
      }
    }
  }
}
