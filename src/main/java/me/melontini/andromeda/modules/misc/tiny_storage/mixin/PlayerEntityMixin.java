package me.melontini.andromeda.modules.misc.tiny_storage.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.misc.tiny_storage.TinyStorage;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PlayerScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin {

    @Shadow @Final public PlayerScreenHandler playerScreenHandler;

    @Shadow @Nullable public abstract ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void andromeda$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtUtil.writeInventoryToNbt("AM-Tiny-Storage", nbt, this.playerScreenHandler.getCraftingInput());
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void andromeda$readNbt(NbtCompound nbt, CallbackInfo ci) {
        try {
            TinyStorage.LOADING.set(true);//We have to skip sending handler updates.
            NbtUtil.readInventoryFromNbt("AM-Tiny-Storage", nbt, this.playerScreenHandler.getCraftingInput());
        } finally {
            TinyStorage.LOADING.remove();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V"), method = "dropInventory")
    private void andromeda$dropAll(CallbackInfo ci) {
        if (ModuleManager.quick(TinyStorage.class).config().transferMode == TinyStorage.TransferMode.ALWAYS_TRANSFER) return;

        for(int i = 0; i < this.playerScreenHandler.getCraftingInput().size(); ++i) {
            ItemStack stack = this.playerScreenHandler.getCraftingInput().removeStack(i);
            if (!stack.isEmpty() && EnchantmentHelper.hasVanishingCurse(stack)) continue;
            this.dropItem(stack, true, false);
        }
    }
}
