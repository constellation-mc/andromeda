package dev.zenfyr.andromeda.modules.items.lava_disintegrator.mixin;

import dev.zenfyr.andromeda.modules.items.lava_disintegrator.client.LavaDisintegratorClient;
import net.minecraft.core.component.DataComponents;
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
      ItemStack self,
      ItemStack other,
      Slot slot,
      ClickAction clickAction,
      Player player,
      SlotAccess carriedItem,
      CallbackInfoReturnable<Boolean> cir) {
    if (clickAction == ClickAction.SECONDARY && self.is(Items.LAVA_BUCKET)) {
      var damageResistant = other.get(DataComponents.DAMAGE_RESISTANT);
      if (damageResistant != null
          && damageResistant.isResistantTo(player.level().damageSources().inFire())) {
        return;
      }

      if (EnchantmentHelper.getItemEnchantmentLevel(
              player.level().registryAccess().getOrThrow(Enchantments.FIRE_PROTECTION), other)
          > 0) return;

      carriedItem.set(ItemStack.EMPTY);
      if (player.level().isClientSide())
        LavaDisintegratorClient.spawnLavaParticles((int) Math.max(2, Math.sqrt(other.getCount())));
      cir.setReturnValue(true);
    }
  }
}
