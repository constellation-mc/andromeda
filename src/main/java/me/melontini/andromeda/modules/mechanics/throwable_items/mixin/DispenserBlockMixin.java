package me.melontini.andromeda.modules.mechanics.throwable_items.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.melontini.andromeda.common.util.ServerHelper;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
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
        map.defaultReturnValue((pointer, stack) -> ItemBehaviorManager.get(pointer.getWorld().getServer()).hasBehaviors(stack.getItem()) ?
                Main.BEHAVIOR.dispense(pointer, stack) : b.dispense(pointer, stack));
    }

    @Inject(at = @At("HEAD"), method = "getBehaviorForItem", cancellable = true)
    private void andromeda$overrideBehavior(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> cir) {
        if (ServerHelper.getContext() == null) return;

        var manager = ItemBehaviorManager.get(ServerHelper.getContext());
        if (manager.hasBehaviors(stack.getItem()) && manager.overridesVanilla(stack.getItem())) {
            cir.setReturnValue(Main.BEHAVIOR);
        }
    }
}
