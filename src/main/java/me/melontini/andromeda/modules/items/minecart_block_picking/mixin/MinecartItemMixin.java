package me.melontini.andromeda.modules.items.minecart_block_picking.mixin;

import me.melontini.andromeda.modules.items.minecart_block_picking.MinecartBlockPicking;
import me.melontini.andromeda.modules.items.minecart_block_picking.PickUpBehaviorHandler;
import me.melontini.andromeda.modules.items.minecart_block_picking.PlaceBehaviorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartItem.class)
abstract class MinecartItemMixin extends Item {

  public MinecartItemMixin(Properties properties) {
    super(properties);
  }

  @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
  public void andromeda$useOnStuff(
      UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockState state = world.getBlockState(pos);
    ItemStack stack = context.getItemInHand();
    Player player = context.getPlayer();
    if (player == null) return;

    if (state.is(BlockTags.RAILS)) {
      RailShape railShape = state.getBlock() instanceof BaseRailBlock
          ? state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty())
          : RailShape.NORTH_SOUTH;
      double d = railShape.isAscending() ? 0.5 : 0.0;

      PlaceBehaviorHandler.getPlaceBehavior(stack.getItem()).ifPresent(b -> {
        if (!world.isClientSide()) {
          AbstractMinecart entity = b.dispense(
              stack, world, pos.getX() + 0.5, pos.getY() + 0.0625, pos.getZ() + 0.5, d, pos);
          if (entity == null) return;

          world.addFreshEntity(entity);
          if (!player.isCreative()) stack.shrink(1);
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide()));
      });
      return;
    }

    if (player.isShiftKeyDown()) {
      if (stack.getItem() != Items.MINECART) return;

      PickUpBehaviorHandler.getPickUpBehavior(state.getBlock()).ifPresent(b -> {
        if (!world.isClientSide()) {
          if (!world.am$get(MinecartBlockPicking.CONFIG).available) return;
          ItemStack stack1 = b.pickUp(state, world, pos);
          if (stack1 == null || stack1.isEmpty()) return;

          if (!player.isCreative()) stack.shrink(1);
          player.getInventory().placeItemBackInInventory(stack1);
          world.destroyBlock(pos, false);
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide()));
      });
    }
  }
}
