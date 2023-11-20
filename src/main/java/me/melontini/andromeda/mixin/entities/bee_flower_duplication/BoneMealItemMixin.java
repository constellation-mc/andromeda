package me.melontini.andromeda.mixin.entities.bee_flower_duplication;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.bee_flower_duplication.BeeFlowerDuplication;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
class BoneMealItemMixin {
    @Unique
    private static final BeeFlowerDuplication am$bfd = ModuleManager.quick(BeeFlowerDuplication.class);

    @Inject(at = @At("HEAD"), method = "useOnFertilizable", cancellable = true)
    private static void andromeda$useOnFertilizable(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!am$bfd.config().enabled || !am$bfd.config().tallFlowers) return;

        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof TallFlowerBlock) {
            if (!world.isClient) {
                if (ModuleManager.get().getModule("misc.unknown").map(m->m.config().enabled).orElse(false) && world.random.nextInt(100) == 0) {
                    world.createExplosion(null, DamageSource.explosion((LivingEntity) null), null,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F,
                            false, Explosion.DestructionType.DESTROY);
                }
            }
            cir.setReturnValue(false);
        }
    }
}
