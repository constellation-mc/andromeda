package me.melontini.andromeda.modules.blocks.bed.power.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.modules.blocks.bed.power.Power;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {

    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=5.0F"), method = "onUse")
    public float andromeda$explosionRedirect(float power, @Local World world) {
        if (world.isClient()) return power;

        var config = world.am$get(Power.class);
        return config.enabled ? config.power : power;
    }
}
