package dev.zenfyr.andromeda.modules.entities.minecarts.items;

import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.util.MakeSure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class AndromedaMinecartItem<T extends AbstractMinecart> extends Item {

  private final Keeper<EntityType<T>> keeper;

  public AndromedaMinecartItem(Keeper<EntityType<T>> keeper, Properties settings) {
    super(settings);
    this.keeper = keeper;
    DispenserBlock.registerBehavior(this, new MinecartDispenseBehavior());
  }

  protected void onCreate(ItemStack stack, T entity) {}

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level world = context.getLevel();
    BlockPos blockPos = context.getClickedPos();
    BlockState blockState = world.getBlockState(blockPos);
    if (!blockState.is(BlockTags.RAILS)) {
      return InteractionResult.FAIL;
    } else {
      ItemStack itemStack = context.getItemInHand();
      if (!world.isClientSide()) {
        RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
            ? blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty())
            : RailShape.NORTH_SOUTH;
        double d = 0.0;
        if (railShape.isSlope()) {
          d = 0.5;
        }

        T minecart =
            MakeSure.notNull(this.keeper.orThrow().create(world, EntitySpawnReason.DISPENSER));
        minecart.setPos(
            (double) blockPos.getX() + 0.5,
            (double) blockPos.getY() + 0.0625 + d,
            (double) blockPos.getZ() + 0.5);

        onCreate(itemStack, minecart);

        var customName = itemStack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
          minecart.setCustomName(customName);
        }

        world.addFreshEntity(minecart);
        world.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
      }

      itemStack.shrink(1);
      return InteractionResult.SUCCESS;
    }
  }

  private class MinecartDispenseBehavior extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

    @Override
    public ItemStack execute(BlockSource pointer, ItemStack stack) {
      Direction direction = pointer.state().getValue(DispenserBlock.FACING);
      Level world = pointer.level();

      BlockPos blockPos = pointer.pos().relative(direction);
      BlockState blockState = world.getBlockState(blockPos);
      RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
          ? blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty())
          : RailShape.NORTH_SOUTH;
      double g;
      if (blockState.is(BlockTags.RAILS)) {
        g = railShape.isSlope() ? 0.6 : 0.1;
      } else {
        if (!blockState.isAir() || !world.getBlockState(blockPos.below()).is(BlockTags.RAILS)) {
          return this.defaultBehavior.dispense(pointer, stack);
        }

        BlockState blockState2 = world.getBlockState(blockPos.below());
        RailShape railShape2 = blockState2.getBlock() instanceof BaseRailBlock
            ? blockState2.getValue(((BaseRailBlock) blockState2.getBlock()).getShapeProperty())
            : RailShape.NORTH_SOUTH;
        g = direction != Direction.DOWN && railShape2.isSlope() ? -0.4 : -0.9;
      }

      double d = pointer.center().x() + direction.getStepX() * 1.125;
      double e = Math.floor(pointer.center().y()) + direction.getStepY();
      double f = pointer.center().z() + direction.getStepZ() * 1.125;

      T minecart = MakeSure.notNull(
          AndromedaMinecartItem.this.keeper.orThrow().create(world, EntitySpawnReason.DISPENSER));
      minecart.setPos(d, e + g, f);

      onCreate(stack, minecart);

      var customName = stack.get(DataComponents.CUSTOM_NAME);
      if (customName != null) {
        minecart.setCustomName(customName);
      }

      world.addFreshEntity(minecart);
      stack.shrink(1);
      return stack;
    }

    @Override
    protected void playSound(BlockSource pointer) {
      pointer.level().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, pointer.pos(), 0);
    }
  }
}
