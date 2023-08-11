package me.melontini.andromeda.blocks.entities;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.blocks.IncubatorBlock;
import me.melontini.andromeda.registries.BlockRegistry;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
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
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IncubatorBlockEntity extends BlockEntity implements SidedInventory {
    public DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public int processingTime = -1;

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.INCUBATOR_BLOCK_ENTITY, pos, state);
    }

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, IncubatorBlockEntity incubatorBlockEntity) {
        incubatorBlockEntity.tick();
    }

    public void tick() {
        assert world != null;
        if (this.processingTime > 0) {
            BlockState state = world.getBlockState(pos.down().down());
            if (!(state.getBlock() instanceof CampfireBlock)) state = world.getBlockState(pos.down());
            if (state.getBlock() instanceof CampfireBlock) {
                if (state.get(CampfireBlock.LIT)) {
                    if (!world.isClient) this.processingTime--;
                    if (world.random.nextInt(4) == 0 && world.isClient) {
                        double i = Utilities.RANDOM.nextDouble(0.6) - 0.3;
                        double j = Utilities.RANDOM.nextDouble(0.6) - 0.3;
                        world.addParticle(ParticleTypes.SMOKE, (pos.getX() + 0.5) + i, pos.getY() + 0.5, (pos.getZ() + 0.5) + j, 0F, 0.07F, 0F);
                    }
                }
            }
        }
        if (!world.isClient()) {
            ItemStack stack = this.inventory.get(0);
            BlockState state = world.getBlockState(this.pos);
            if (!stack.isEmpty() && this.processingTime == -1) {
                EggProcessingData data = Andromeda.EGG_DATA.get(stack.getItem());
                if (data != null) {
                    this.processingTime = Andromeda.CONFIG.incubatorSettings.incubatorRandomness ? (int) (data.time + (Math.random() * (data.time * 0.3) * 2) - data.time * 0.3) : data.time;
                    world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
                    markDirty();
                }
            } else if (stack.isEmpty() && this.processingTime != -1) {
                this.processingTime = -1;
                world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
                markDirty();
            }

            if (this.processingTime == 0) {
                if (Andromeda.EGG_DATA.containsKey(stack.getItem())) {
                    EggProcessingData data = Andromeda.EGG_DATA.get(stack.getItem());
                    Entity entity = Registries.ENTITY_TYPE.get(Identifier.tryParse(data.entity)).create(world);
                    BlockPos entityPos = pos.offset(state.get(IncubatorBlock.FACING));
                    if (entity != null) {
                        entity.setPos(entityPos.getX() + 0.5, entityPos.getY() + 0.5, entityPos.getZ() + 0.5);
                        if (entity instanceof PassiveEntity passive) passive.setBaby(true);
                        stack.decrement(1);
                        world.spawnEntity(entity);
                    }
                    this.processingTime = -1;
                    world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
                    markDirty();
                }
            }
        }
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

    public boolean takeEgg(PlayerEntity player, ItemStack stack) {
        if (!stack.isEmpty()) {
            return false;
        } else {
            if (!this.inventory.get(0).isEmpty()) {
                player.getInventory().offerOrDrop(this.inventory.get(0));
                this.inventory.set(0, ItemStack.EMPTY);
                markDirty();
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean insertEgg(ItemStack stack) {
        ItemStack stack1 = stack.copy();
        ItemStack slot = this.inventory.get(0);
        if (slot.isEmpty()) {
            this.inventory.set(0, stack1);
            stack.setCount(0);
            markDirty();
            return true;
        } else if (slot.getItem() == stack.getItem()) {
            int a = slot.getCount();
            int b = stack1.getCount();
            if (a + b <= slot.getMaxCount()) {
                this.inventory.set(0, stack1);
                this.inventory.get(0).setCount(a + b);
                stack.setCount(0);
                markDirty();
            } else if (a + b > slot.getMaxCount()) {
                int c = a + b;
                this.inventory.set(0, stack1);
                this.inventory.get(0).setCount(stack1.getMaxCount());
                stack.setCount(MathHelper.clamp(c - stack1.getMaxCount(), 0, stack1.getMaxCount()));
                markDirty();
            }
            return true;
        }
        return false;
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
        assert world != null;
        return dir != world.getBlockState(this.pos).get(IncubatorBlock.FACING) && Andromeda.EGG_DATA.containsKey(stack.getItem());
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        assert world != null;
        return dir != world.getBlockState(this.pos).get(IncubatorBlock.FACING);
    }
}
