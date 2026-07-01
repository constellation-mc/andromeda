package dev.zenfyr.andromeda.modules.items.mending_tweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.modules.items.mending_tweaks.Utils;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(AnvilScreen.class)
abstract class AnvilScreenMixin extends AbstractContainerScreen<AnvilMenu> {

  public AnvilScreenMixin(AnvilMenu handler, Inventory inventory, Component title) {
    super(handler, inventory, title);
  }

  @ModifyExpressionValue(
      method = "extractLabels",
      at = @At(value = "CONSTANT", args = "intValue=40"))
  private int andromeda$setRepairLimit(int constant) {
    if (!this.menu.getSlot(1).getItem().is(Items.ENCHANTED_BOOK)) {
      if (Utils.hasMending(this.menu.getSlot(0).getItem())) {
        return Integer.MAX_VALUE;
      }
    }
    return constant;
  }
}
