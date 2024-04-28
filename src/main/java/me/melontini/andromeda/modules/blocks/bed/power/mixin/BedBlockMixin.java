package me.melontini.andromeda.modules.blocks.bed.power.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.modules.blocks.bed.power.Power;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.loot.context.LootContextParameters.*;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {

    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=5.0F"), method = "onUse")
    public float andromeda$explosionRedirect(float power, @Local(argsOnly = true) World world, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) PlayerEntity player, @Local(argsOnly = true) Hand hand) {
        if (world.isClient()) return power;

        var config = world.am$get(Power.class);
        return config.e.enabled ? config.c.power.asFloat(() -> {
            LootContextParameterSet set = new LootContextParameterSet.Builder((ServerWorld) world)
                    .add(ORIGIN, Vec3d.ofCenter(pos))
                    .add(BLOCK_STATE, state)
                    .add(THIS_ENTITY, player)
                    .add(TOOL, player.getStackInHand(hand))
                    .build(LootContextTypes.BLOCK);

            return new LootContext.Builder(set).build(null);
        }) : power;
    }
}
