package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.event.EventContext;
import me.melontini.commander.api.event.EventKey;
import me.melontini.commander.api.event.EventType;
import me.melontini.commander.api.expression.Arithmetica;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.data.nbt.NbtUtil;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public class IncubatorBlockEntity extends BlockEntity implements SidedInventory {

    public DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public int processingTime = -1;

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(Main.INCUBATOR_BLOCK_ENTITY.get(), pos, state);
    }

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, IncubatorBlockEntity incubatorBlockEntity) {
        incubatorBlockEntity.tick(world, state);
    }

    public void tick(World world, BlockState state) {
        if (this.processingTime > 0) this.tickProcessingTime(world);

        if (world.isClient()) return;
        ItemStack stack = this.inventory.get(0);
        if (!stack.isEmpty() && this.processingTime == -1) {
            EggProcessingData data = requireNonNull(world.getServer()).dm$getReloader(EggProcessingData.RELOADER).get(stack.getItem());
            if (data != null) {
                int time = getTime(data.time(), stack);
                this.processingTime = world.am$get(Incubator.CONFIG).randomness ? (time + MathUtil.nextInt(time / -3, time / 3)) : time;
                this.update(state);
            }
        } else if (stack.isEmpty() && this.processingTime != -1) {
            this.processingTime = -1;
            this.update(state);
        }

        if (this.processingTime == 0) this.spawnResult(stack, (ServerWorld) world, state);
    }

    private int getTime(Arithmetica arithmetica, ItemStack stack) {
        if (arithmetica.toSource().left().isPresent()) return arithmetica.asInt(null);

        var supplier = LootContextUtil.block(world, Vec3d.ofCenter(getPos()), getCachedState(), stack, null, this);
        return arithmetica.asInt(supplier.get());
    }

    private void spawnResult(ItemStack stack, ServerWorld world, BlockState state) {
        EggProcessingData data = world.getServer().dm$getReloader(EggProcessingData.RELOADER).get(stack.getItem());
        if (data != null) {
            EggProcessingData.Entry entry = data.entity().shuffle().stream().findFirst().orElseThrow();
            Entity entity = entry.type().create(world);
            if (entity != null) {
                entity.readNbt(entry.nbt());
                BlockPos entityPos = pos.offset(state.get(IncubatorBlock.FACING));
                entity.setPos(entityPos.getX() + 0.5, entityPos.getY() + 0.5, entityPos.getZ() + 0.5);
                if (entity instanceof PassiveEntity passive) passive.setBaby(true);

                world.spawnEntity(entity);
                executeCommands(entry, world, stack, entity);

                stack.decrement(1);
            }
        }
        this.processingTime = -1;
        this.update(state);
    }

    private void executeCommands(EggProcessingData.Entry entry, ServerWorld world, ItemStack stack, Entity entity) {
        if (entry.commands().isEmpty()) return;

        var supplier = LootContextUtil.block(world, Vec3d.ofCenter(getPos()), getCachedState(), stack, entity, this);
        EventContext context = EventContext.builder(EventType.NULL).addParameter(EventKey.LOOT_CONTEXT, supplier.get()).build();
        for (Command.Conditioned command : entry.commands()) {
            command.execute(context);
        }
    }

    private boolean isLitCampfire(BlockState state) {
        if (!(state.getBlock() instanceof CampfireBlock)) return false;
        return state.get(CampfireBlock.LIT);
    }

    private void tickProcessingTime(World world) {
        BlockState state = world.getBlockState(pos.down());
        if (!isLitCampfire(state)) state = world.getBlockState(pos.down().down());
        if (!isLitCampfire(state)) return;

        if (world.isClient && world.random.nextInt(4) == 0) {
            double i = MathUtil.threadRandom().nextDouble(0.6) - 0.3;
            double j = MathUtil.threadRandom().nextDouble(0.6) - 0.3;
            world.addParticle(ParticleTypes.SMOKE, (pos.getX() + 0.5) + i, pos.getY() + 0.5, (pos.getZ() + 0.5) + j, 0F, 0.07F, 0F);
            return;
        }
        if (!world.isClient) this.processingTime--;
    }

    private void update(BlockState state) {
        MakeSure.notNull(world).updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        markDirty();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    public ActionResult insertEgg(ItemStack stack) {
        try (Transaction transaction = Transaction.openOuter()) {
            var storage = InventoryStorage.of(this, null);
            long i = StorageUtil.tryInsertStacking(storage, ItemVariant.of(stack), stack.getCount(), transaction);
            if (i > 0) {
                transaction.commit();
                this.markDirty();
                stack.setCount((int) (stack.getCount() - i));
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }
    }

    public ActionResult extractEgg(PlayerEntity player) {
        try (Transaction transaction = Transaction.openOuter()) {
            var storage = InventoryStorage.of(this, null);
            var ra = StorageUtil.extractAny(storage, Long.MAX_VALUE, transaction);
            if (ra != null && ra.amount() > 0) {
                transaction.commit();
                this.markDirty();
                player.getInventory().offerOrDrop(ra.resource().toStack((int) ra.amount()));
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.processingTime = nbt.getInt("ProcessingTime");
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        NbtUtil.readInventoryFromNbt(nbt, this);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("ProcessingTime", this.processingTime);
        NbtUtil.writeInventoryToNbt(nbt, this);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.inventory, slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return dir != MakeSure.notNull(world).getBlockState(this.pos).get(IncubatorBlock.FACING) &&
                requireNonNull(Andromeda.get().getCurrentServer())
                        .dm$getReloader(EggProcessingData.RELOADER).get(stack.getItem()) != null;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir != Objects.requireNonNull(world).getBlockState(this.pos).get(IncubatorBlock.FACING);
    }
}
