package dev.zenfyr.andromeda.modules.items.tooltips.mixin.compass;

import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.items.tooltips.Tooltips;
import dev.zenfyr.pulsar.api.util.MathUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
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
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay tooltipDisplay,
      Consumer<Component> tooltipAdder,
      TooltipFlag flag,
      CallbackInfo ci) {
    if (!AndromedaClient.CLIENT.get(Tooltips.CONFIG).compass) return;
    var world = Minecraft.getInstance().level;

    if (world != null)
      if (world.isClientSide()) {
        var player = Minecraft.getInstance().player;
        if (stack.getItem() == Items.COMPASS && player != null) {
          boolean lodestone = stack.has(DataComponents.LODESTONE_TRACKER);
          GlobalPos globalPos = lodestone
              ? stack.get(DataComponents.LODESTONE_TRACKER).target().orElse(null)
              : world.getRespawnData().globalPos();

          double dist;
          if (globalPos != null && world.dimension() == globalPos.dimension()) {
            Vec3 compassPos = new Vec3(
                globalPos.pos().getX() + 0.5,
                globalPos.pos().getY() + 0.5,
                globalPos.pos().getZ() + 0.5);
            dist = MiscUtil.horizontalDistanceTo(player.position(), compassPos);
          } else {
            dist = MathUtil.threadRandom().nextGaussian() * 0.1;
          }
          tooltipAdder.accept(TextUtil.translatable(
                  lodestone ? "tooltip.andromeda.compass.lodestone" : "tooltip.andromeda.compass",
                  String.format("%.1f", dist))
              .withStyle(ChatFormatting.GRAY));
        }
      }
  }
}
