package me.melontini.andromeda.mixin.blocks.better_fletching_table;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.better_fletching_table.BetterFletchingTable;
import me.melontini.andromeda.modules.blocks.better_fletching_table.FletchingScreenHandler;
import me.melontini.andromeda.util.AndromedaTexts;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FletchingTableBlock.class)
class FletchingTableBlockMixin extends CraftingTableBlock {
    @Unique
    private static final BetterFletchingTable am$bft = ModuleManager.quick(BetterFletchingTable.class);

    public FletchingTableBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
    private void andromeda$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!am$bft.config().enabled) return;

        if (state.isOf(Blocks.FLETCHING_TABLE)) {
            if (player.world.isClient) {
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player1) -> new FletchingScreenHandler(syncId, inv, ScreenHandlerContext.create(world, pos)), AndromedaTexts.FLETCHING_SCREEN));
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
