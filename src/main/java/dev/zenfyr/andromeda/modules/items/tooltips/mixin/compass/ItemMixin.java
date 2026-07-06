package dev.zenfyr.andromeda.modules.items.tooltips.mixin.compass;

import dev.zenfyr.andromeda.common.client.AndromedaClient;
import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.items.tooltips.Tooltips;
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
      ItemStack itemStack,
      Item.TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> builder,
      TooltipFlag tooltipFlag,
      CallbackInfo ci) {
    if (!AndromedaClient.CLIENT.get(Tooltips.CONFIG).compass) return;
    var level = Minecraft.getInstance().level;
    if (level == null) return;

    var player = Minecraft.getInstance().player;
    if (itemStack.getItem() != Items.COMPASS || player == null) return;

    boolean lodestone = itemStack.has(DataComponents.LODESTONE_TRACKER);
    String key = lodestone ? "tooltip.andromeda.compass.lodestone" : "tooltip.andromeda.compass";
    GlobalPos globalPos = lodestone
        ? itemStack.get(DataComponents.LODESTONE_TRACKER).target().orElse(null)
        : level.getRespawnData().globalPos();

    double dist;
    if (globalPos != null && level.dimension() == globalPos.dimension()) {
      Vec3 compassPos = new Vec3(
          globalPos.pos().getX() + 0.5,
          globalPos.pos().getY() + 0.5,
          globalPos.pos().getZ() + 0.5);
      dist = MiscUtil.horizontalDistanceTo(player.position(), compassPos);
    } else {
      dist = -1;
    }

    var component = TextUtil.literal(String.format("%.1f", dist));
    if (dist == -1) component.withStyle(ChatFormatting.OBFUSCATED);
    builder.accept(TextUtil.translatable(key, component).withStyle(ChatFormatting.GRAY));
  }
}
