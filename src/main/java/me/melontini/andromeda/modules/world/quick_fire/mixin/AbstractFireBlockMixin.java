package me.melontini.andromeda.modules.world.quick_fire.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.ScopedConfigs;
import me.melontini.andromeda.modules.world.quick_fire.QuickFire;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FireBlock.class)
abstract class AbstractFireBlockMixin extends AbstractFireBlock {

    @Shadow protected abstract void trySpreadingFire(World world, BlockPos pos, int spreadFactor, Random random, int currentAge);

    public AbstractFireBlockMixin(Settings settings, float damage) {
        super(settings, damage);
    }

    @ModifyVariable(method = "trySpreadingFire", at = @At(value = "LOAD"), index = 3, argsOnly = true)
    public int andromeda$spreadFire0(int value, @Local World world) {
        return ScopedConfigs.get(world, QuickFire.class).enabled ? (int) (value * 0.8) : value;
    }

    @ModifyExpressionValue(method = "trySpreadingFire", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int andromeda$spreadFire01(int value, @Local World world) {
        return ScopedConfigs.get(world, QuickFire.class).enabled ? (int) Math.ceil(value / 3d) : value;
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/block/FireBlock.trySpreadingFire (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;I)V", ordinal = 0, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, method = "scheduledTick")
    public void andromeda$trySpreadBlocks(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci, BlockState blockState, boolean bl, int i, int j, boolean bl2, int k) {
        if (ScopedConfigs.get(world, QuickFire.class).enabled) {
            for (int x = -3; x < 3; x++) {
                for (int y = -3; y < 3; y++) {
                    for (int z = -3; z < 3; z++) {
                        this.trySpreadingFire(world, new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z), 300 + k, random, i);
                    }
                }
            }
        }
    }
}
