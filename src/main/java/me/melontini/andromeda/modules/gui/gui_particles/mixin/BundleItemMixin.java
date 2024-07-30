package me.melontini.andromeda.modules.gui.gui_particles.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BundleItem.class)
abstract class BundleItemMixin {

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/component/type/BundleContentsComponent$Builder;add(Lnet/minecraft/item/ItemStack;)I"),
      method = "onClicked",
      index = 0)
  private ItemStack andromeda$spawnParticlesClicked(
      ItemStack stack, @Share("original") LocalRef<ItemStack> ref) {
    ref.set(stack.copy());
    return stack;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/component/type/BundleContentsComponent$Builder;add(Lnet/minecraft/item/ItemStack;)I"),
      method = "onClicked")
  private int andromeda$spawnParticlesClicked(
      int original, @Share("original") LocalRef<ItemStack> other) {
    if (original > 0) this.andromeda$renderParticles(other.get());
    return original;
  }

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/component/type/BundleContentsComponent$Builder;add(Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/entity/player/PlayerEntity;)I"),
      method = "onStackClicked",
      index = 0)
  private Slot andromeda$spawnParticlesStackClicked(
      Slot slot, @Share("original") LocalRef<ItemStack> other) {
    other.set(slot.getStack().copy());
    return slot;
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/component/type/BundleContentsComponent$Builder;add(Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/entity/player/PlayerEntity;)I"),
      method = "onStackClicked")
  private int andromeda$spawnParticlesStackClicked(
      int original, @Share("original") LocalRef<ItemStack> other) {
    if (original > 0) this.andromeda$renderParticles(other.get());
    return original;
  }

  @Unique private void andromeda$renderParticles(ItemStack stack) {
    if (AndromedaClient.HANDLER.get(GuiParticles.CONFIG).bundleInputParticles) {
      var client = MinecraftClient.getInstance();
      if (client.isOnThread() && client.currentScreen != null) {
        int x = (int) (client.mouse.getX()
            * (double) client.getWindow().getScaledWidth()
            / (double) client.getWindow().getWidth());
        int y = (int) (client.mouse.getY()
            * (double) client.getWindow().getScaledHeight()
            / (double) client.getWindow().getHeight());
        ScreenParticleHelper.addScreenParticles(
            new ItemStackParticleEffect(ParticleTypes.ITEM, stack), x, y, 0.5, 0.5, 0.1, 7);
      }
    }
  }
}
