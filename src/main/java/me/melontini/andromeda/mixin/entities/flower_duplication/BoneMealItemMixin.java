package me.melontini.andromeda.mixin.entities.flower_duplication;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
@MixinRelatedConfigOption({"beeFlowerDuplication", "beeTallFlowerDuplication"})
public class BoneMealItemMixin {
    @Inject(at = @At("HEAD"), method = "useOnFertilizable", cancellable = true)
    private static void andromeda$useOnFertilizable(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!Andromeda.CONFIG.beeTallFlowerDuplication) return;

        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof TallFlowerBlock) {
            if (!world.isClient) {
                if (Andromeda.CONFIG.unknown && world.random.nextInt(100) == 0) {
                    world.createExplosion(null, DamageSource.explosion((LivingEntity) null), null,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F,
                            false, Explosion.DestructionType.DESTROY);
                }
            }
            cir.setReturnValue(false);
        }
    }
}
