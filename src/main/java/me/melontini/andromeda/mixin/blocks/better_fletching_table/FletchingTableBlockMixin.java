package me.melontini.andromeda.mixin.blocks.better_fletching_table;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.screens.FletchingScreenHandler;
import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.FletchingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FletchingTableBlock.class)
@MixinRelatedConfigOption("usefulFletching")
public class FletchingTableBlockMixin extends CraftingTableBlock {
    public FletchingTableBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
    private void andromeda$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!Config.get().usefulFletching) return;

        if (state.isOf(Blocks.FLETCHING_TABLE)) {
            if (player.world.isClient)
                cir.setReturnValue(ActionResult.SUCCESS);

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player1) -> new FletchingScreenHandler(syncId, inv, ScreenHandlerContext.create(world, pos)), AndromedaTexts.FLETCHING_SCREEN));
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
