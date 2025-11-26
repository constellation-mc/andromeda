package dev.zenfyr.andromeda.modules.blocks.incubator;

import static java.util.Objects.requireNonNull;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.LootContextBuilder;
import dev.zenfyr.andromeda.modules.blocks.incubator.data.EggProcessingData;
import dev.zenfyr.pulsar.nbt.NbtUtil;
import dev.zenfyr.pulsar.util.MakeSure;
import dev.zenfyr.pulsar.util.MathUtil;
import java.util.Objects;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.event.EventContext;
import me.melontini.commander.api.event.EventKey;
import me.melontini.commander.api.event.EventType;
import me.melontini.commander.api.expression.Arithmetica;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class IncubatorBlockEntity extends BlockEntity implements WorldlyContainer {

  public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
  public int processingTime = -1;

  public IncubatorBlockEntity(BlockPos pos, BlockState state) {
    super(IncubatorBlock.INCUBATOR_BLOCK_ENTITY.get(), pos, state);
  }

  @SuppressWarnings("unused")
  public static void tick(
      Level world, BlockPos pos, BlockState state, IncubatorBlockEntity incubatorBlockEntity) {
    incubatorBlockEntity.tick(world, state);
  }

  public void tick(Level world, BlockState state) {
    if (this.processingTime > 0) this.tickProcessingTime(world);

    if (world.isClientSide()) return;
    ItemStack stack = this.inventory.get(0);
    if (!stack.isEmpty() && this.processingTime == -1) {
      EggProcessingData data = requireNonNull(world.getServer())
          .pulsar$getReloader(EggProcessingData.RELOADER)
          .get(stack.getItem());
      if (data != null) {
        int time = getTime(data.time(), stack);
        this.processingTime = world.am$get(Incubator.CONFIG).randomness
            ? (time + MathUtil.nextInt(time / -3, time / 3))
            : time;
        this.update(state);
      }
    } else if (stack.isEmpty() && this.processingTime != -1) {
      this.processingTime = -1;
      this.update(state);
    }

    if (this.processingTime == 0) this.spawnResult(stack, (ServerLevel) world, state);
  }

  private int getTime(Arithmetica arithmetica, ItemStack stack) {
    if (arithmetica.toSource().left().isPresent()) return arithmetica.asInt(null);

    var supplier = LootContextBuilder.block(
        level,
        builder ->
            builder.origin(worldPosition).state(getBlockState()).tool(stack).blockEntity(this));
    return arithmetica.asInt(supplier.get());
  }

  private void spawnResult(ItemStack stack, ServerLevel world, BlockState state) {
    EggProcessingData data =
        world.getServer().pulsar$getReloader(EggProcessingData.RELOADER).get(stack.getItem());
    if (data != null) {
      EggProcessingData.Entry entry =
          data.entity().shuffle().stream().findFirst().orElseThrow();
      Entity entity = entry.type().create(world);
      if (entity != null) {
        entity.load(entry.nbt());
        BlockPos entityPos = worldPosition.relative(state.getValue(IncubatorBlock.FACING));
        entity.setPosRaw(entityPos.getX() + 0.5, entityPos.getY() + 0.5, entityPos.getZ() + 0.5);
        if (entity instanceof AgeableMob passive) passive.setBaby(true);

        world.addFreshEntity(entity);
        executeCommands(entry, world, stack, entity);

        stack.shrink(1);
      }
    }
    this.processingTime = -1;
    this.update(state);
  }

  private void executeCommands(
      EggProcessingData.Entry entry, ServerLevel world, ItemStack stack, Entity entity) {
    if (entry.commands().isEmpty()) return;

    var supplier = LootContextBuilder.block(world, builder -> builder
        .origin(getBlockPos())
        .state(getBlockState())
        .tool(stack)
        .thisEntity(entity)
        .blockEntity(this));
    EventContext context = EventContext.builder(EventType.NULL)
        .addParameter(EventKey.LOOT_CONTEXT, supplier.get())
        .build();
    for (Command.Conditioned command : entry.commands()) {
      command.execute(context);
    }
  }

  private boolean isLitCampfire(BlockState state) {
    if (!(state.getBlock() instanceof CampfireBlock)) return false;
    return state.getValue(CampfireBlock.LIT);
  }

  private void tickProcessingTime(Level world) {
    BlockState state = world.getBlockState(worldPosition.below());
    if (!isLitCampfire(state)) state = world.getBlockState(worldPosition.below().below());
    if (!isLitCampfire(state)) return;

    if (world.isClientSide && world.random.nextInt(4) == 0) {
      double i = MathUtil.threadRandom().nextDouble(0.6) - 0.3;
      double j = MathUtil.threadRandom().nextDouble(0.6) - 0.3;
      world.addParticle(
          ParticleTypes.SMOKE,
          (worldPosition.getX() + 0.5) + i,
          worldPosition.getY() + 0.5,
          (worldPosition.getZ() + 0.5) + j,
          0F,
          0.07F,
          0F);
      return;
    }
    if (!world.isClientSide) this.processingTime--;
  }

  private void update(BlockState state) {
    MakeSure.notNull(level).sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    setChanged();
  }

  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag nbt = new CompoundTag();
    this.saveAdditional(nbt);
    return nbt;
  }

  public InteractionResult insertEgg(ItemStack stack) {
    try (Transaction transaction = Transaction.openOuter()) {
      var storage = InventoryStorage.of(this, null);
      long i = StorageUtil.tryInsertStacking(
          storage, ItemVariant.of(stack), stack.getCount(), transaction);
      if (i > 0) {
        transaction.commit();
        this.setChanged();
        stack.setCount((int) (stack.getCount() - i));
        return InteractionResult.SUCCESS;
      }
      return InteractionResult.CONSUME;
    }
  }

  public InteractionResult extractEgg(Player player) {
    try (Transaction transaction = Transaction.openOuter()) {
      var storage = InventoryStorage.of(this, null);
      var ra = StorageUtil.extractAny(storage, Long.MAX_VALUE, transaction);
      if (ra != null && ra.amount() > 0) {
        transaction.commit();
        this.setChanged();
        player.getInventory().placeItemBackInInventory(ra.resource().toStack((int) ra.amount()));
        return InteractionResult.SUCCESS;
      }
      return InteractionResult.CONSUME;
    }
  }

  @Override
  public void load(CompoundTag nbt) {
    super.load(nbt);
    this.processingTime = nbt.getInt("ProcessingTime");
    this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    NbtUtil.readInventoryFromNbt(nbt, this);
  }

  @Override
  public void saveAdditional(CompoundTag nbt) {
    super.saveAdditional(nbt);
    nbt.putInt("ProcessingTime", this.processingTime);
    NbtUtil.writeInventoryToNbt(nbt, this);
  }

  @Override
  public int getContainerSize() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return this.inventory.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int slot) {
    return inventory.get(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    ItemStack itemStack = ContainerHelper.removeItem(this.inventory, slot, amount);
    if (!itemStack.isEmpty()) {
      this.setChanged();
    }

    return itemStack;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    return ContainerHelper.takeItem(this.inventory, slot);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    this.inventory.set(slot, stack);
    if (stack.getCount() > this.getMaxStackSize()) {
      stack.setCount(this.getMaxStackSize());
    }
  }

  @Override
  public boolean stillValid(Player player) {
    return false;
  }

  @Override
  public void clearContent() {
    inventory.clear();
  }

  @Override
  public int[] getSlotsForFace(Direction side) {
    return new int[] {0};
  }

  @Override
  public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
    return dir
            != MakeSure.notNull(level)
                .getBlockState(this.worldPosition)
                .getValue(IncubatorBlock.FACING)
        && requireNonNull(Andromeda.get().getCurrentServer())
                .pulsar$getReloader(EggProcessingData.RELOADER)
                .get(stack.getItem())
            != null;
  }

  @Override
  public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
    return dir
        != Objects.requireNonNull(level)
            .getBlockState(this.worldPosition)
            .getValue(IncubatorBlock.FACING);
  }
}
