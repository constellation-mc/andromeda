package dev.zenfyr.andromeda.modules.gui.gui_particles.mixin;

import dev.zenfyr.andromeda.common.AndromedaClient;
import dev.zenfyr.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {

  public AnvilScreenHandlerMixin(
      @Nullable MenuType<?> type,
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context) {
    super(type, syncId, playerInventory, context);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
              ordinal = 0),
      method = "onTake")
  private void andromeda$particles(Player player, ItemStack stack, CallbackInfo ci) {
    if (!AndromedaClient.CLIENT.get(GuiParticles.CONFIG).anvilScreenParticles) return;

    if (Minecraft.getInstance().isSameThread()
        && Minecraft.getInstance().screen instanceof AnvilScreen anvilScreen) {
      BlockState state = Blocks.ANVIL.defaultBlockState();
      var slot = this.slots.get(2);
      boolean enchant = this.slots.get(1).getItem().is(Items.ENCHANTED_BOOK);
      ScreenParticleHelper.addScreenParticles(
          !enchant ? new BlockParticleOption(ParticleTypes.BLOCK, state) : ParticleTypes.END_ROD,
          anvilScreen.leftPos + slot.x + 8,
          anvilScreen.topPos + slot.y + 8,
          0.5,
          0.5,
          !enchant ? 0.5 : 0.07,
          5);
    }
  }
}
