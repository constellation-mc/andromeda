package me.melontini.andromeda.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.UseWithItemHack;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
abstract class AbstractBlockStateMixin {

  @ModifyReturnValue(method = "onUseWithItem", at = @At("RETURN"))
  private ItemActionResult catchUseContext(
      ItemActionResult original,
      @Local(argsOnly = true) ItemStack stack,
      @Local(argsOnly = true) Hand hand) {
    if (original == ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION)
      UseWithItemHack.setContext(new UseWithItemHack.Context(stack, hand));
    return original;
  }

  @Inject(at = @At("RETURN"), method = "onUse")
  private void resetUseContext(
      World world,
      PlayerEntity player,
      BlockHitResult hit,
      CallbackInfoReturnable<ActionResult> cir) {
    if (UseWithItemHack.getContext() != null) UseWithItemHack.setContext(null);
  }
}
