package me.melontini.andromeda.items.minecarts;

import me.melontini.crackerutil.util.TextUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MinecartItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnerMinecartItem extends MinecartItem {

    public static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {
        public final ItemDispenserBehavior defaultBehavior = new ItemDispenserBehavior();

        @Override
        public ItemStack dispenseSilently(@NotNull BlockPointer pointer, ItemStack stack) {
            Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
            World world = pointer.getWorld();
            double d = pointer.getX() + direction.getOffsetX() * 1.125;
            double e = Math.floor(pointer.getY()) + direction.getOffsetY();
            double f = pointer.getZ() + direction.getOffsetZ() * 1.125;
            BlockPos blockPos = pointer.getPos().offset(direction);
            BlockState blockState = world.getBlockState(blockPos);
            RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock ? blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double k;
            if (blockState.isIn(BlockTags.RAILS)) {
                if (railShape.isAscending()) {
                    k = 0.6;
                } else {
                    k = 0.1;
                }
            } else {
                if (!blockState.isAir() || !world.getBlockState(blockPos.down()).isIn(BlockTags.RAILS)) {
                    return this.defaultBehavior.dispense(pointer, stack);
                }

                BlockState blockState2 = world.getBlockState(blockPos.down());
                RailShape railShape2 = blockState2.getBlock() instanceof AbstractRailBlock ? blockState2.get(((AbstractRailBlock) blockState2.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railShape2.isAscending()) {
                    k = -0.4;
                } else {
                    k = -0.9;
                }
            }

            SpawnerMinecartEntity spawnerMinecartEntity = (SpawnerMinecartEntity) AbstractMinecartEntity.create(world, d, e + k, f, AbstractMinecartEntity.Type.SPAWNER);

            NbtCompound nbt = stack.getNbt();
            if (nbt != null) if (nbt.getString("Entity") != null) {
                spawnerMinecartEntity.getLogic().setEntityId(Registry.ENTITY_TYPE.get(Identifier.tryParse(nbt.getString("Entity"))));
            }

            if (stack.hasCustomName()) {
                spawnerMinecartEntity.setCustomName(stack.getName());
            }

            world.spawnEntity(spawnerMinecartEntity);
            stack.decrement(1);
            return stack;
        }

        @Override
        public void playSound(@NotNull BlockPointer pointer) {
            pointer.getWorld().syncWorldEvent(1000, pointer.getPos(), 0);
        }
    };

    public SpawnerMinecartItem(AbstractMinecartEntity.Type type, FabricItemSettings properties) {
        super(type, properties);
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) if (nbt.getString("Entity") != null) {
            tooltip.add(TextUtil.translatable("tooltip.andromeda.spawner_minecart.filled", Registry.ENTITY_TYPE.get(Identifier.tryParse(nbt.getString("Entity"))).getName()).formatted(Formatting.GRAY));
        }
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();
        if (!blockState.isIn(BlockTags.RAILS)) {
            return ActionResult.FAIL;
        } else {
            ItemStack itemStack = context.getStack();
            if (!world.isClient) {
                RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock ? blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                double d = 0.0;
                if (railShape.isAscending()) {
                    d = 0.5;
                }

                SpawnerMinecartEntity spawnerMinecartEntity = (SpawnerMinecartEntity) AbstractMinecartEntity.create(world, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 0.0625 + d, (double) blockPos.getZ() + 0.5, this.type);

                NbtCompound nbt = stack.getNbt();
                if (nbt != null) if (nbt.getString("Entity") != null) {
                    spawnerMinecartEntity.getLogic().setEntityId(Registry.ENTITY_TYPE.get(Identifier.tryParse(nbt.getString("Entity"))));
                }

                if (itemStack.hasCustomName()) {
                    spawnerMinecartEntity.setCustomName(itemStack.getName());
                }

                world.spawnEntity(spawnerMinecartEntity);
            }

            if (player != null && !player.isCreative()) itemStack.decrement(1);
            return ActionResult.success(world.isClient);
        }
    }
}
