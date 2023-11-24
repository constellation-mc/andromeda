package me.melontini.andromeda.mixin.blocks.bed.safe;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;
import me.melontini.andromeda.util.AndromedaTexts;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.BedBlock.isBedWorking;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {
    @Unique
    private static final Safe am$safe = ModuleManager.quick(Safe.class);

    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
    public void andromeda$onUse(BlockState state, @NotNull World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient || !am$safe.config().enabled) return;

        if (!isBedWorking(world)) {
            player.sendMessage(AndromedaTexts.SAFE_BEDS, true);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
