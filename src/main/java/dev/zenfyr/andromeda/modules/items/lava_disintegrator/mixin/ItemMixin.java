package dev.zenfyr.andromeda.modules.items.lava_disintegrator.mixin;

import dev.zenfyr.andromeda.modules.items.lava_disintegrator.client.LavaDisintegratorClient;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
abstract class ItemMixin {

  @Inject(at = @At("HEAD"), method = "overrideOtherStackedOnMe", cancellable = true)
  private void andromeda$onLavaClick(
      ItemStack stack,
      ItemStack otherStack,
      Slot slot,
      ClickAction clickType,
      Player player,
      SlotAccess cursorStackReference,
      CallbackInfoReturnable<Boolean> cir) {
    if (clickType == ClickAction.SECONDARY && stack.is(Items.LAVA_BUCKET)) {
      if (otherStack.getItem().isFireResistant()
          || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_PROTECTION, otherStack)
              > 0) return;

      cursorStackReference.set(ItemStack.EMPTY);
      if (player.level.isClientSide())
        LavaDisintegratorClient.spawnLavaParticles(
            (int) Math.max(2, Math.sqrt(otherStack.getCount())));
      cir.setReturnValue(true);
    }
  }
}
