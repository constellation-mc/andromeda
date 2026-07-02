package dev.zenfyr.andromeda.modules.items.tooltips.mixin.clock;

import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.items.tooltips.Tooltips;
import dev.zenfyr.pulsar.api.util.MathUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
abstract class ItemMixin {

  @Inject(at = @At("HEAD"), method = "appendHoverText")
  public void andromeda$tooltip(
      ItemStack stack,
      @Nullable Level world,
      List<Component> tooltip,
      TooltipFlag context,
      CallbackInfo ci) {
    if (!AndromedaClient.CLIENT.get(Tooltips.CONFIG).clock) return;

    if (world != null && world.isClientSide()) {
      if (stack.getItem() == Items.CLOCK) {
        // totally not stolen from here
        // https://bukkit.org/threads/how-can-i-convert-minecraft-long-time-to-real-hours-and-minutes.122912/
        int i = MathUtil.fastFloor((world.getDayTime() / 1000d + 8) % 24);
        int j = MathUtil.fastFloor(60 * (world.getDayTime() % 1000d) / 1000);
        tooltip.add(
            TextUtil.translatable("tooltip.andromeda.clock", String.format("%02d:%02d", i, j))
                .withStyle(ChatFormatting.GRAY));
      }
    }
  }
}
