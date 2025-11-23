package me.melontini.andromeda.modules.gui.gui_particles.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.AndromedaClient;
import me.melontini.andromeda.modules.gui.gui_particles.CustomItemStackParticle;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
abstract class CreativeInventoryScreenMixin
    extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
  public CreativeInventoryScreenMixin(
          CreativeModeInventoryScreen.ItemPickerMenu screenHandler,
          Inventory playerInventory,
          Component text) {
    super(screenHandler, playerInventory, text);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                      "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleCreativeModeItemAdd(Lnet/minecraft/world/item/ItemStack;I)V",
              ordinal = 0,
              shift = At.Shift.BEFORE),
      method = "slotClicked")
  private void andromeda$clickDeleteParticles(
          Slot slot,
          int slotId,
          int button,
          ClickType actionType,
          CallbackInfo ci,
          @Local(ordinal = 2) int index) {
    var config = AndromedaClient.CLIENT.get(GuiParticles.CONFIG);
    if (!config.creativeScreenParticles) return;

    if (index >= this.menu.slots.size()) return;
    Slot slot1 = this.menu.slots.get(index);
    ScreenParticleHelper.addScreenParticle(new CustomItemStackParticle(
        this.leftPos + slot1.x + 8,
        this.topPos + slot1.y + 8,
        MathUtil.nextDouble(
            -config.creativeScreenParticlesVelX, config.creativeScreenParticlesVelX),
        0.6,
        slot1.getItem()));
  }
}
