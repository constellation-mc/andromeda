package dev.zenfyr.andromeda.modules.gui.gui_particles.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.gui.gui_particles.GuiParticles;
import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
abstract class BundleItemMixin {

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/component/BundleContents$Mutable;tryTransfer(Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/entity/player/Player;)I"),
      method = "overrideStackedOnOther",
      index = 0)
  private Slot andromeda$spawnParticlesClicked(
      Slot slot, @Share("original") LocalRef<ItemStack> ref) {
    ref.set(slot.getItem().copy());
    return slot;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/BundleItem;playInsertSound(Lnet/minecraft/world/entity/Entity;)V"),
      method = "overrideStackedOnOther")
  private void andromeda$spawnParticlesClicked(
      ItemStack itemStack,
      Slot slot,
      ClickAction clickAction,
      Player player,
      CallbackInfoReturnable<Boolean> cir,
      @Share("original") LocalRef<ItemStack> other) {
    this.andromeda$renderParticles(other.get());
  }

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/component/BundleContents$Mutable;tryInsert(Lnet/minecraft/world/item/ItemStack;)I"),
      method = "overrideOtherStackedOnMe",
      index = 0)
  private ItemStack andromeda$spawnParticlesStackClicked(
      ItemStack stack, @Share("original") LocalRef<ItemStack> other) {
    other.set(stack.copy());
    return stack;
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/BundleItem;playInsertSound(Lnet/minecraft/world/entity/Entity;)V"),
      method = "overrideOtherStackedOnMe")
  private void andromeda$spawnParticlesStackClicked(
      ItemStack itemStack,
      ItemStack itemStack2,
      Slot slot,
      ClickAction clickAction,
      Player player,
      SlotAccess slotAccess,
      CallbackInfoReturnable<Boolean> cir,
      @Share("original") LocalRef<ItemStack> other) {
    this.andromeda$renderParticles(other.get());
  }

  @Unique private void andromeda$renderParticles(ItemStack stack) {
    if (AndromedaClient.CLIENT.get(GuiParticles.CONFIG).bundleInputParticles) {
      var client = Minecraft.getInstance();
      if (client.isSameThread() && client.screen != null) {
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
                    new ItemParticleOption(ParticleTypes.ITEM, stack), x, y, 0.5, 0.5, 0.1, 7));
      }
    }
  }
}
