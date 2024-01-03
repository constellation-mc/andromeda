package me.melontini.andromeda.modules.mechanics.throwable_items.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.melontini.andromeda.modules.mechanics.throwable_items.Content;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
abstract class DispenserBlockMixin {

    @Inject(at = @At("TAIL"), method = "method_10008")
    private static void andromeda$throwItem(Object2ObjectOpenHashMap<Item, DispenserBehavior> map, CallbackInfo ci) {
        var b = map.defaultReturnValue();
        map.defaultReturnValue((pointer, stack) -> ItemBehaviorManager.hasBehaviors(stack.getItem()) ?
                Content.BEHAVIOR.dispense(pointer, stack) : b.dispense(pointer, stack));
    }

    @Inject(at = @At("HEAD"), method = "getBehaviorForItem", cancellable = true)
    private void andromeda$overrideBehavior(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> cir) {
        if (ItemBehaviorManager.hasBehaviors(stack.getItem()) && ItemBehaviorManager.overridesVanilla(stack.getItem())) {
            cir.setReturnValue(Content.BEHAVIOR);
        }
    }
}
