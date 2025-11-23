package me.melontini.andromeda.modules.items.tooltips.mixin.compass;

import java.util.List;
import me.melontini.andromeda.common.AndromedaClient;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.items.tooltips.Tooltips;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.phys.Vec3;
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
    if (!AndromedaClient.CLIENT.get(Tooltips.CONFIG).compass) return;

    if (world != null)
      if (world.isClientSide) {
        var player = Minecraft.getInstance().player;
        if (stack.getItem() == Items.COMPASS && player != null) {
          boolean lodestone = stack.hasTag() && CompassItem.isLodestoneCompass(stack);
          GlobalPos globalPos = lodestone
              ? CompassItem.getLodestonePosition(stack.getTag())
              : CompassItem.getSpawnPosition(world);

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
          tooltip.add(TextUtil.translatable(
                  lodestone ? "tooltip.andromeda.compass.lodestone" : "tooltip.andromeda.compass",
                  String.format("%.1f", dist))
              .withStyle(ChatFormatting.GRAY));
        }
      }
  }
}
