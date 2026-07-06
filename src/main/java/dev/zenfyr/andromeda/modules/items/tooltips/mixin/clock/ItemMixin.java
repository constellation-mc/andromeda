package dev.zenfyr.andromeda.modules.items.tooltips.mixin.clock;

import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.modules.items.tooltips.Tooltips;
import dev.zenfyr.pulsar.api.util.MathUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
abstract class ItemMixin {

  @Inject(at = @At("HEAD"), method = "appendHoverText")
  public void andromeda$tooltip(
      ItemStack itemStack,
      Item.TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> builder,
      TooltipFlag tooltipFlag,
      CallbackInfo ci) {
    if (!AndromedaClient.CLIENT.get(Tooltips.CONFIG).clock) return;
    var level = Minecraft.getInstance().level;
    if (level == null) return;
    if (itemStack.getItem() != Items.CLOCK) return;

    int hour;
    int minute;

    // mimics vanilla item model behavior
    boolean hasClock = level.dimension().equals(Level.OVERWORLD);
    if (hasClock) {
      // totally not stolen from here
      // https://bukkit.org/threads/how-can-i-convert-minecraft-long-time-to-real-hours-and-minutes.122912/
      hour = MathUtil.fastFloor((level.getDefaultClockTime() / 1000d + 8) % 24);
      minute = MathUtil.fastFloor(60 * (level.getDefaultClockTime() % 1000d) / 1000);
    } else {
      hour = 12;
      minute = 30;
    }

    var component = TextUtil.literal(String.format("%02d:%02d", hour, minute));
    if (!hasClock) component.withStyle(ChatFormatting.OBFUSCATED);
    builder.accept(
        TextUtil.translatable("tooltip.andromeda.clock", component).withStyle(ChatFormatting.GRAY));
  }
}
