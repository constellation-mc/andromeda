package dev.zenfyr.andromeda.modules.gui.gui_particles.mixin;

import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.gui.gui_particles.GuiParticles;
import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreen.class)
abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
  public EnchantmentScreenMixin(EnchantmentMenu handler, Inventory inventory, Component title) {
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
      MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {
    if (!AndromedaClient.CLIENT.get(GuiParticles.CONFIG).enchantmentScreenParticles) return;

    Slot slot = this.menu.slots.get(0);
    ScreenParticles.get(Minecraft.getInstance())
        .addParticles(
            this,
            VanillaParticles.create(
                ParticleTypes.END_ROD,
                this.leftPos + slot.x + 8,
                this.topPos + slot.y + 8,
                0.5,
                0.5,
                0.07,
                10));
  }
}
