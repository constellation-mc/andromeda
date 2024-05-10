package me.melontini.andromeda.modules.world.quick_fire.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.world.quick_fire.QuickFire;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
abstract class AbstractFireBlockMixin extends AbstractFireBlock {

    @Shadow protected abstract void trySpreadingFire(World world, BlockPos pos, int spreadFactor, Random random, int currentAge);
    @Unique private static final ThreadLocal<Boolean> LOCAL = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public AbstractFireBlockMixin(Settings settings, float damage) {
        super(settings, damage);
    }

    @ModifyVariable(method = "trySpreadingFire", at = @At("LOAD"), index = 3, argsOnly = true)
    public int andromeda$spreadFire0(int value, @Local(argsOnly = true) World world, @Local(argsOnly = true) BlockPos pos) {
        return Boolean.TRUE.equals(LOCAL.get()) ? (int) (value * 0.8) : value;
    }

    @ModifyExpressionValue(method = "trySpreadingFire", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int andromeda$spreadFire01(int value, @Local(argsOnly = true) World world) {
        return Boolean.TRUE.equals(LOCAL.get()) ? (int) Math.ceil(value / 3d) : value;
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/block/FireBlock.trySpreadingFire (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;I)V", ordinal = 0, shift = At.Shift.BEFORE), method = "scheduledTick")
    public void andromeda$trySpreadBlocks(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci, @Local(index = 7) int i, @Local(index = 10) int k) {
        if (world.am$get(QuickFire.CONFIG).available.asBoolean(LootContextUtil.command(world, Vec3d.ofCenter(pos)))) {
            try {
                LOCAL.set(Boolean.TRUE);
                for (int x = -3; x < 3; x++) {
                    for (int y = -3; y < 3; y++) {
                        for (int z = -3; z < 3; z++) {
                            this.trySpreadingFire(world, new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z), 300 + k, random, i);
                        }
                    }
                }
            } finally {
                LOCAL.remove();
            }
        }
    }
}
