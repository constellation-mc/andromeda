package me.melontini.andromeda.mixin.blocks.bed.power;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
@Feature("enableBedExplosionPower")
abstract class BedBlockMixin extends Block {

    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=5.0F"), method = "onUse")
    public float andromeda$explosionRedirect(float power) {
        return Config.get().enableBedExplosionPower ? Config.get().bedExplosionPower : power;
    }
}
