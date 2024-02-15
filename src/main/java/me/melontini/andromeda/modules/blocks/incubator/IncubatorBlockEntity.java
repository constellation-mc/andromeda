package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
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
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IncubatorBlockEntity extends BlockEntity implements SidedInventory {

    public DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public int processingTime = -1;
    private final Incubator module = ModuleManager.quick(Incubator.class);

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
            EggProcessingData data = EggProcessingData.EGG_DATA.get(stack.getItem());
            if (data != null) {
                this.processingTime = module.config().randomness ? (data.time() + MathStuff.nextInt(data.time() / -3, data.time() / 3)) : data.time();
                this.update(state);
            }
        } else if (stack.isEmpty() && this.processingTime != -1) {
            this.processingTime = -1;
            this.update(state);
        }

        if (this.processingTime == 0) this.spawnResult(stack, world, state);
    }

    private void spawnResult(ItemStack stack, World world, BlockState state) {
        EggProcessingData data = EggProcessingData.EGG_DATA.get(stack.getItem());
        if (data != null) {
            Entity entity = data.entity().create(world);
            if (entity != null) {
                BlockPos entityPos = pos.offset(state.get(IncubatorBlock.FACING));
                entity.setPos(entityPos.getX() + 0.5, entityPos.getY() + 0.5, entityPos.getZ() + 0.5);
                if (entity instanceof PassiveEntity passive) passive.setBaby(true);
                stack.decrement(1);
                world.spawnEntity(entity);
            }
        }
        this.processingTime = -1;
        this.update(state);
    }

    private boolean isLitCampfire(BlockState state) {
        if (!(state.getBlock() instanceof CampfireBlock)) return false;
        return state.get(CampfireBlock.LIT);
    }

    private void tickProcessingTime(World world) {
        BlockState state = world.getBlockState(pos.down());
        if (isLitCampfire(state)) state = world.getBlockState(pos.down().down());
        if (isLitCampfire(state)) return;

        if (world.isClient && world.random.nextInt(4) == 0) {
            double i = MathStuff.threadRandom().nextDouble(0.6) - 0.3;
            double j = MathStuff.threadRandom().nextDouble(0.6) - 0.3;
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
        return dir != MakeSure.notNull(world).getBlockState(this.pos).get(IncubatorBlock.FACING) && EggProcessingData.EGG_DATA.containsKey(stack.getItem());
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir != MakeSure.notNull(world).getBlockState(this.pos).get(IncubatorBlock.FACING);
    }
}
