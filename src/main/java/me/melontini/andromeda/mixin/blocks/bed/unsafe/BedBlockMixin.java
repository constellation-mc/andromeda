package me.melontini.andromeda.mixin.blocks.bed.unsafe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.bed.unsafe.Unsafe;
import net.minecraft.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
class BedBlockMixin {
    @Unique
    private static final Unsafe am$un = ModuleManager.quick(Unsafe.class);

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"), method = "onUse")
    private boolean andromeda$explode(boolean original) {
        return !am$un.config().enabled;
    }
}
