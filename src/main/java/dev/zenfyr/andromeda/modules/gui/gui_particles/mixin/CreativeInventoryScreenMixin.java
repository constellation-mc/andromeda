package dev.zenfyr.andromeda.modules.gui.gui_particles.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.gui.gui_particles.CustomItemStackParticle;
import dev.zenfyr.andromeda.modules.gui.gui_particles.GuiParticles;
import dev.zenfyr.pulsar.api.client.particles.ScreenParticleHelper;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
abstract class CreativeInventoryScreenMixin
    extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

  public CreativeInventoryScreenMixin(
      CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu,
      Inventory inventory,
      Component component) {
    super(abstractContainerMenu, inventory, component);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/inventory/Slot;set(Lnet/minecraft/world/item/ItemStack;)V",
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
