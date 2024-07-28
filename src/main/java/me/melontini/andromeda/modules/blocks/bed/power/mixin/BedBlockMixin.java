package me.melontini.andromeda.modules.blocks.bed.power.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.blocks.bed.power.Power;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {

    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=5.0F"), method = "onUse")
    public float andromeda$explosionRedirect(float power, @Local(argsOnly = true) World world, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) PlayerEntity player, @Local(argsOnly = true) Hand hand) {
        if (world.isClient()) return power;
        var supplier = Memoize.supplier(LootContextUtil.block(world, Vec3d.ofCenter(pos), state, player.getStackInHand(hand), player));
        var config = world.am$get(Power.CONFIG);
        return config.available.asBoolean(supplier) ? config.power.asFloat(supplier) : power;
    }
}
