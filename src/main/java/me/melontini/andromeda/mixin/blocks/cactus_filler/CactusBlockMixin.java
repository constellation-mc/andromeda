package me.melontini.andromeda.mixin.blocks.cactus_filler;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.BlockUtil;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CactusBlock.class)
@MixinRelatedConfigOption("cactusBottleFilling")
class CactusBlockMixin {
    @Inject(at = @At("TAIL"), method = "appendProperties")
    private void andromeda$appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        if (Config.get().cactusBottleFilling) builder.add(BlockUtil.WATER_LEVEL_3);
    }
}
