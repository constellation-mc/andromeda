package dev.zenfyr.andromeda.modules.blocks.incubator;

import static java.util.Objects.requireNonNull;
import static dev.zenfyr.andromeda.common.Andromeda.id;

import java.util.List;
import java.util.Set;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.util.AndromedaItemGroup;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.blocks.incubator.data.EggProcessingData;
import dev.zenfyr.andromeda.modules.misc.unknown.Unknown;
import dev.zenfyr.andromeda.util.Util;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class IncubatorBlock extends BaseEntityBlock implements WorldlyContainerHolder {

  public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
  public static final Keeper<IncubatorBlock> INCUBATOR_BLOCK = Keeper.create();
  public static final Keeper<BlockItem> INCUBATOR = Keeper.create();
  public static final Keeper<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY =
      Keeper.create();
  private final VoxelShape BASE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 11.0, 15.0);
  private final VoxelShape GLASS_SHAPE = Block.box(3.0, 11.0, 3.0, 13.0, 18.0, 13.0);

  public IncubatorBlock(Properties settings) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, INCUBATOR_BLOCK_ENTITY.orThrow(), IncubatorBlockEntity::tick);
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level world,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    ItemStack stack = player.getItemInHand(hand);
    IncubatorBlockEntity entity = (IncubatorBlockEntity) world.getBlockEntity(pos);
    if (world.isClientSide || entity == null || !hand.equals(InteractionHand.MAIN_HAND))
      return InteractionResult.sidedSuccess(true);

    if (requireNonNull(world.getServer())
            .dm$getReloader(EggProcessingData.RELOADER)
            .get(stack.getItem())
        != null) return entity.insertEgg(stack);
    if (stack.isEmpty()) return entity.extractEgg(player);

    return InteractionResult.sidedSuccess(false);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options) {
    if (ModuleManager.get().get(Unknown.class).isPresent())
      tooltip.add(
          TextUtil.translatable("tooltip.andromeda.incubator[1]").withStyle(ChatFormatting.GRAY));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext ctx) {
    return this.defaultBlockState()
        .setValue(FACING, ctx.getHorizontalDirection().getOpposite());
  }

  @Override
  public void onRemove(
      BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
    if (!state.is(newState.getBlock())) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof IncubatorBlockEntity incubatorBlockEntity) {
        if (!world.isClientSide) {
          Containers.dropContents(world, pos, incubatorBlockEntity);
        }
        world.updateNeighbourForOutputSignal(pos, this);
      }

      super.onRemove(state, world, pos, newState, moved);
    }
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(FACING)));
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
    return Shapes.or(BASE_SHAPE, GLASS_SHAPE);
  }

  @Nullable @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new IncubatorBlockEntity(pos, state);
  }

  @Override
  public boolean isPathfindable(
      BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
    return false;
  }

  @Override
  public WorldlyContainer getContainer(BlockState state, LevelAccessor world, BlockPos pos) {
    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof IncubatorBlockEntity incubatorBlockEntity)
      return incubatorBlockEntity;
    throw Util.create(
        "Invalid block entity type! Must be an instance of %s"
            .formatted(IncubatorBlockEntity.class.getName()),
        IllegalStateException::new);
  }

  public static void init() {
    var module = ModuleManager.get().get(Incubator.class).orElseThrow();
    IncubatorBlock.INCUBATOR_BLOCK.init(RegistryUtil.register(
        BuiltInRegistries.BLOCK,
        id("incubator"),
        () -> new IncubatorBlock(
            FabricBlockSettings.create().strength(2.0F, 3.0F).sound(SoundType.WOOD))));
    IncubatorBlock.INCUBATOR.init(RegistryUtil.register(
        BuiltInRegistries.ITEM,
        id("incubator"),
        () -> new BlockItem(IncubatorBlock.INCUBATOR_BLOCK.orThrow(), new FabricItemSettings())));
    IncubatorBlock.INCUBATOR_BLOCK_ENTITY.init(RegistryUtil.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        id("incubator"),
        () -> new BlockEntityType<>(
            IncubatorBlockEntity::new, Set.of(IncubatorBlock.INCUBATOR_BLOCK.orThrow()), null)));

    AndromedaItemGroup.BUS.listen(acceptor ->
        acceptor.keeper(module, CreativeModeTabs.FUNCTIONAL_BLOCKS, IncubatorBlock.INCUBATOR));

    EggProcessingData.init();
  }
}
