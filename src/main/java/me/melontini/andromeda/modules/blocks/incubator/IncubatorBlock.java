package me.melontini.andromeda.modules.blocks.incubator;

import com.mojang.serialization.MapCodec;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.andromeda.modules.misc.unknown.Unknown;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class IncubatorBlock extends BlockWithEntity implements InventoryProvider {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private final VoxelShape BASE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 11.0, 15.0);
    private final VoxelShape GLASS_SHAPE = Block.createCuboidShape(3.0, 11.0, 3.0, 13.0, 18.0, 13.0);
    public static final MapCodec<IncubatorBlock> CODEC = createCodec(IncubatorBlock::new);

    public IncubatorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, Main.INCUBATOR_BLOCK_ENTITY.orThrow(), IncubatorBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        IncubatorBlockEntity entity = (IncubatorBlockEntity) world.getBlockEntity(pos);
        if (world.isClient || entity == null || !hand.equals(Hand.MAIN_HAND)) return ActionResult.success(true);

        if (EggProcessingData.EGG_DATA.containsKey(stack.getItem())) return entity.insertEgg(stack);
        if (stack.isEmpty()) return entity.extractEgg(player);

        return ActionResult.success(false);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (ModuleManager.get().getModule(Unknown.class).isPresent())
            tooltip.add(TextUtil.translatable("tooltip.andromeda.incubator[1]").formatted(Formatting.GRAY));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof IncubatorBlockEntity incubatorBlockEntity) {
                if (!world.isClient) {
                    ItemScatterer.spawn(world, pos, incubatorBlockEntity);
                }
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(BASE_SHAPE, GLASS_SHAPE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IncubatorBlockEntity(pos, state);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof IncubatorBlockEntity incubatorBlockEntity) return incubatorBlockEntity;
        throw AndromedaException.builder().message("Invalid block entity type! Must be an instance of %s".formatted(IncubatorBlockEntity.class.getName()))
                .add("block_entity", blockEntity).build();
    }
}
