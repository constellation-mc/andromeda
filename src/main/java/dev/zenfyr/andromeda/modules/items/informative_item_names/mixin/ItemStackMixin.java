package dev.zenfyr.andromeda.modules.items.informative_item_names.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.pulsar.api.util.TextUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/ItemStack;getStyledHoverName()Lnet/minecraft/network/chat/Component;",
              ordinal = 0),
      method = "getTooltipLines")
  private Component andromeda$getTooltip(Component original) {
    MutableComponent mutable = original instanceof MutableComponent m ? m : original.copy();
    ItemStack self = (ItemStack) (Object) this;

    if (!self.has(DataComponents.MAX_DAMAGE)) {
      if (self.getCount() > 1)
        mutable.append(
            TextUtil.literal(" x" + self.getCount()).withStyle(self.getRarity().color()));
    } else {
      if (self.getDamageValue() > 0)
        mutable.append(TextUtil.literal(" "
                + ((self.getMaxDamage() - self.getDamageValue()) * 100 / self.getMaxDamage()) + "%")
            .withStyle(self.getRarity().color()));
    }
    return mutable;
  }
}
