package dev.zenfyr.andromeda.modules.items.tooltips.mixin.recovery_compass;

import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.items.tooltips.Tooltips;
import dev.zenfyr.pulsar.api.util.MathUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;
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
    if (!AndromedaClient.CLIENT.get(Tooltips.CONFIG).recoveryCompass) return;
    var world = Minecraft.getInstance().level;

    if (world != null)
      if (world.isClientSide()) {
        var player = Minecraft.getInstance().player;
        if (itemStack.getItem() == Items.RECOVERY_COMPASS && player != null) {
          var optional = player.getLastDeathLocation();
          if (optional.isPresent()) {
            GlobalPos globalPos = optional.get();

            double dist;
            if (world.dimension() == globalPos.dimension()) {
              Vec3 compassPos = new Vec3(
                  globalPos.pos().getX() + 0.5,
                  globalPos.pos().getY() + 0.5,
                  globalPos.pos().getZ() + 0.5);
              dist = MiscUtil.horizontalDistanceTo(player.position(), compassPos);
            } else {
              dist = MathUtil.threadRandom().nextGaussian() * 0.1;
            }
            builder.accept(TextUtil.translatable(
                    "tooltip.andromeda.compass.recovery", String.format("%.1f", dist))
                .withStyle(ChatFormatting.GRAY));
          }
        }
      }
  }
}
