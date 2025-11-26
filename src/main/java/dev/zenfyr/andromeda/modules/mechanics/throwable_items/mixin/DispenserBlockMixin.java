package dev.zenfyr.andromeda.modules.mechanics.throwable_items.mixin;

import static dev.zenfyr.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.Main;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
abstract class DispenserBlockMixin {

  @Inject(at = @At("TAIL"), method = "method_10008")
  private static void andromeda$throwItem(
      Object2ObjectOpenHashMap<Item, DispenseItemBehavior> map, CallbackInfo ci) {
    var b = map.defaultReturnValue();
    map.defaultReturnValue((pointer, stack) ->
        pointer.getLevel().getServer().pulsar$getReloader(RELOADER).hasBehaviors(stack)
            ? Main.BEHAVIOR.dispense(pointer, stack)
            : b.dispense(pointer, stack));
  }

  @Inject(at = @At("HEAD"), method = "getDispenseMethod", cancellable = true)
  private void andromeda$overrideBehavior(
      ItemStack stack, CallbackInfoReturnable<DispenseItemBehavior> cir) {
    var server = Andromeda.get().getCurrentServer();
    if (server == null) return;

    var manager = server.pulsar$getReloader(RELOADER);
    if (manager.hasBehaviors(stack) && manager.overridesVanilla(stack.getItem())) {
      cir.setReturnValue(Main.BEHAVIOR);
    }
  }
}
