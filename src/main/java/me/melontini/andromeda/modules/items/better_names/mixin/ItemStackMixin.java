package me.melontini.andromeda.modules.items.better_names.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
              ordinal = 0,
              shift = At.Shift.BEFORE),
      method = "getTooltipLines")
  private void andromeda$getTooltip(
          @Nullable Player player,
          TooltipFlag context,
          CallbackInfoReturnable<List<Component>> cir,
          @Local MutableComponent mutableText) {
    if (!this.getItem().canBeDepleted()) {
      if (this.getCount() > 1)
        mutableText.append(
            TextUtil.literal(" x" + this.getCount()).withStyle(getRarity().color));
    } else {
      if (this.getDamageValue() > 0)
        mutableText.append(TextUtil.literal(
                " " + ((this.getMaxDamage() - this.getDamageValue()) * 100 / this.getMaxDamage()) + "%")
            .withStyle(getRarity().color));
    }
  }
}
