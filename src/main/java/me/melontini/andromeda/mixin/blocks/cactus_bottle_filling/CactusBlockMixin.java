package me.melontini.andromeda.mixin.blocks.cactus_bottle_filling;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.cactus_bottle_filling.CactusFiller;
import me.melontini.andromeda.util.BlockUtil;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CactusBlock.class)
@Feature("cactusBottleFilling")
class CactusBlockMixin {
    @Unique
    private static final CactusFiller am$cbf = ModuleManager.quick(CactusFiller.class);

    @Inject(at = @At("TAIL"), method = "appendProperties")
    private void andromeda$appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        if (am$cbf.config().enabled) builder.add(BlockUtil.WATER_LEVEL_3);
    }
}
