package me.melontini.andromeda.modules.entities.minecarts.items;

import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class AndromedaMinecartItem<T extends AbstractMinecartEntity> extends Item {

    private final Keeper<EntityType<T>> keeper;

    public AndromedaMinecartItem(Keeper<EntityType<T>> keeper, Settings settings) {
        super(settings);
        this.keeper = keeper;
        DispenserBlock.registerBehavior(this, new MinecartDispenseBehavior());
    }

    protected void onCreate(ItemStack stack, T entity) { }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.isIn(BlockTags.RAILS)) {
            return ActionResult.FAIL;
        } else {
            ItemStack itemStack = context.getStack();
            if (!world.isClient) {
                RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock
                        ? blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                double d = 0.0;
                if (railShape.isAscending()) {
                    d = 0.5;
                }

                T minecart = MakeSure.notNull(this.keeper.orThrow().create(world));
                minecart.setPosition((double) blockPos.getX() + 0.5, (double) blockPos.getY() + 0.0625 + d, (double) blockPos.getZ() + 0.5);

                onCreate(itemStack, minecart);

                if (itemStack.hasCustomName()) {
                    minecart.setCustomName(itemStack.getName());
                }

                world.spawnEntity(minecart);
                world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
            }

            itemStack.decrement(1);
            return ActionResult.success(world.isClient);
        }
    }

    private class MinecartDispenseBehavior extends ItemDispenserBehavior {
        private final ItemDispenserBehavior defaultBehavior = new ItemDispenserBehavior();

        @Override
        public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction direction = pointer.state().get(DispenserBlock.FACING);
            World world = pointer.world();

            BlockPos blockPos = pointer.pos().offset(direction);
            BlockState blockState = world.getBlockState(blockPos);
            RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock
                    ? blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty())
                    : RailShape.NORTH_SOUTH;
            double g;
            if (blockState.isIn(BlockTags.RAILS)) {
                g = railShape.isAscending() ? 0.6 : 0.1;
            } else {
                if (!blockState.isAir() || !world.getBlockState(blockPos.down()).isIn(BlockTags.RAILS)) {
                    return this.defaultBehavior.dispense(pointer, stack);
                }

                BlockState blockState2 = world.getBlockState(blockPos.down());
                RailShape railShape2 = blockState2.getBlock() instanceof AbstractRailBlock
                        ? blockState2.get(((AbstractRailBlock) blockState2.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                g = direction != Direction.DOWN && railShape2.isAscending() ? -0.4 : -0.9;
            }

            double d = pointer.centerPos().getX() + direction.getOffsetX() * 1.125;
            double e = Math.floor(pointer.centerPos().getY()) + direction.getOffsetY();
            double f = pointer.centerPos().getZ() + direction.getOffsetZ() * 1.125;

            T minecart = MakeSure.notNull(AndromedaMinecartItem.this.keeper.orThrow().create(world));
            minecart.setPosition(d, e + g, f);

            onCreate(stack, minecart);

            if (stack.hasCustomName()) {
                minecart.setCustomName(stack.getName());
            }

            world.spawnEntity(minecart);
            stack.decrement(1);
            return stack;
        }

        @Override
        protected void playSound(BlockPointer pointer) {
            pointer.world().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.pos(), 0);
        }
    }
}
