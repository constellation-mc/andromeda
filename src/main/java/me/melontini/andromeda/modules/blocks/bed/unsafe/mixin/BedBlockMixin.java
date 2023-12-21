package me.melontini.andromeda.modules.blocks.bed.unsafe.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.modules.blocks.bed.unsafe.Unsafe;
import net.minecraft.block.BedBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
class BedBlockMixin {

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"), method = "onUse")
    private boolean andromeda$explode(boolean original, @Local World world) {
        if (world.isClient()) return original;

        var config = world.am$get(Unsafe.class);
        return !config.enabled && original;
    }
}
