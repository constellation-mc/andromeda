package me.melontini.andromeda.mixin.blocks.bed.unsafe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
@MixinRelatedConfigOption("bedsExplodeEverywhere")
public class BedBlockMixin {

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"), method = "onUse")
    private boolean andromeda$explode(boolean original) {
        return !Andromeda.CONFIG.bedsExplodeEverywhere;
    }
}
