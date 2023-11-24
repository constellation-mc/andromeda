package me.melontini.andromeda.mixin.blocks.bed.power;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.bed.power.Power;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {
    @Unique
    private static final Power am$pow = ModuleManager.quick(Power.class);

    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=5.0F"), method = "onUse")
    public float andromeda$explosionRedirect(float power) {
        return am$pow.config().enabled ? am$pow.config().power : power;
    }
}
