package me.melontini.andromeda.modules.items.minecart_block_picking.mixin;

import me.melontini.andromeda.modules.items.minecart_block_picking.PickUpBehaviorHandler;
import me.melontini.andromeda.modules.items.minecart_block_picking.PlaceBehaviorHandler;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartItem.class)
abstract class MinecartItemMixin extends Item {

    public MinecartItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    public void andromeda$useOnStuff(@NotNull ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();
        if (player == null) return;

        if (state.isIn(BlockTags.RAILS)) {
            RailShape railShape = state.getBlock() instanceof AbstractRailBlock ? state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = railShape.isAscending() ? 0.5 : 0.0;

            PlaceBehaviorHandler.getPlaceBehavior(stack.getItem()).ifPresent(b -> {
                if (!world.isClient()) {
                    AbstractMinecartEntity entity = b.dispense(stack, world, pos.getX() + 0.5, pos.getY() + 0.0625, pos.getZ() + 0.5, d, pos);
                    if (entity == null) return;

                    world.spawnEntity(entity);
                    if (!player.isCreative()) stack.decrement(1);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
            });
            return;
        }

        if (player.isSneaking()) {
            if (stack.getItem() != Items.MINECART) return;

            PickUpBehaviorHandler.getPickUpBehavior(state.getBlock()).ifPresent(b -> {
                if (!world.isClient()) {
                    ItemStack stack1 = b.pickUp(state, world, pos);
                    if (stack1 == null || stack1.isEmpty()) return;

                    if (!player.isCreative()) stack.decrement(1);
                    player.getInventory().offerOrDrop(stack1);
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
            });
        }
    }
}
