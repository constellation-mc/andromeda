package dev.zenfyr.andromeda.modules.items.informative_item_names.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.pulsar.api.util.TextUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

  @Shadow
  public abstract int getMaxDamage();

  @Shadow
  public abstract Item getItem();

  @Shadow
  public abstract int getCount();

  @Shadow
  public abstract int getDamageValue();

  @Shadow
  public abstract Rarity getRarity();

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

    if (!((ItemStack) (Object) this).has(DataComponents.MAX_DAMAGE)) {
      if (this.getCount() > 1)
        mutable.append(
            TextUtil.literal(" x" + this.getCount()).withStyle(getRarity().color()));
    } else {
      if (this.getDamageValue() > 0)
        mutable.append(TextUtil.literal(" "
                + ((this.getMaxDamage() - this.getDamageValue()) * 100 / this.getMaxDamage()) + "%")
            .withStyle(getRarity().color()));
    }
    return mutable;
  }
}
