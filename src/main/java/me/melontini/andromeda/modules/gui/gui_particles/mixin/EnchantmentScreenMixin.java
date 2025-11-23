package me.melontini.andromeda.modules.gui.gui_particles.mixin;

import me.melontini.andromeda.common.AndromedaClient;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreen.class)
abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
  public EnchantmentScreenMixin(
          EnchantmentMenu handler, Inventory inventory, Component title) {
    super(handler, inventory, title);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                      "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryButtonClick(II)V",
              shift = At.Shift.AFTER),
      method = "mouseClicked")
  private void andromeda$particles(
      double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    if (!AndromedaClient.CLIENT.get(GuiParticles.CONFIG).enchantmentScreenParticles) return;

    Slot slot = this.menu.slots.get(0);
    ScreenParticleHelper.addScreenParticles(
        ParticleTypes.END_ROD, this.leftPos + slot.x + 8, this.topPos + slot.y + 8, 0.5, 0.5, 0.07, 10);
  }
}
