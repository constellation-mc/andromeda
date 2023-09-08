package me.melontini.andromeda.entity.vehicle.minecarts;

import me.melontini.andromeda.registries.EntityTypeRegistry;
import me.melontini.andromeda.registries.ItemRegistry;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NoteBlockMinecartEntity extends AbstractMinecartEntity {
    public int note = 0;
    public boolean isPowered = false;

    public NoteBlockMinecartEntity(EntityType<? extends NoteBlockMinecartEntity> entityType, World world) {
        super(entityType, world);
    }

    public NoteBlockMinecartEntity(World world, double x, double y, double z) {
        super(EntityTypeRegistry.get().NOTEBLOCK_MINECART_ENTITY, world, x, y, z);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        this.playNote(world, new Vec3d(getX(), getY() - 1, getZ()));
        super.damage(source, amount);
        return true;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        this.cycleNote();
        this.playNote(world, new Vec3d(getX(), getY() - 1, getZ()));
        player.incrementStat(Stats.TUNE_NOTEBLOCK);
        return ActionResult.success(world.isClient);
    }

    @Override
    public void onActivatorRail(int x, int y, int z, boolean powered) {
        if (powered && !this.isPowered) {
            playNote(this.world, new Vec3d(getX(), getY() - 1, getZ()));
        }
    }

    @Override
    public void tick() {
        int i = MathHelper.floor(this.getX());
        int j = MathHelper.floor(this.getY());
        int k = MathHelper.floor(this.getZ());
        if (this.world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }

        BlockPos blockPos = new BlockPos(i, j, k);
        BlockState blockState = this.world.getBlockState(blockPos);
        if (AbstractRailBlock.isRail(blockState)) {
            if (blockState.isOf(Blocks.ACTIVATOR_RAIL)) {
                if (blockState.get(PoweredRailBlock.POWERED)) {
                    this.onActivatorRail(i, j, k, true);
                    this.isPowered = true;
                } else {
                    this.isPowered = false;
                }
            } else {
                this.isPowered = false;
            }
        }
        super.tick();
    }

    @Override
    public Type getMinecartType() {
        return Type.CHEST;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.note = nbt.getInt("Note");
        this.isPowered = nbt.getBoolean("Powered");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Note", this.note);
        nbt.putBoolean("Powered", this.isPowered);
    }

    @Override
    public Item getItem() {
        return ItemRegistry.get().NOTE_BLOCK_MINECART;
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return Blocks.NOTE_BLOCK.getDefaultState();
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(ItemRegistry.get().NOTE_BLOCK_MINECART);
    }

    public void cycleNote() {
        int nextNote = this.note + 1;
        if (nextNote < Properties.NOTE.stream().toList().size()) {
            this.note = nextNote;
        } else {
            this.note = 0;
        }
    }

    public void playNote(World world, Vec3d pos) {
        BlockPos blockPos = new BlockPos(MathHelper.floor(pos.getX()), MathHelper.floor(pos.getY()), MathHelper.floor(pos.getZ()));
        //BlockState state = world.getBlockState(blockPos);

        Instrument instrument = world.getBlockState(blockPos.up()).getInstrument();
        if (!instrument.isNotBaseBlock()) {
            Instrument instrument2 = world.getBlockState(blockPos.down()).getInstrument();
            instrument = instrument2.isNotBaseBlock() ? Instrument.HARP : instrument2;
        }

        int i = this.note;
        float f = (float) Math.pow(2.0, (i - 12) / 12.0);
        this.world.playSound(null, new BlockPos(MathHelper.floor(pos.getX()), MathHelper.floor(pos.getY()), MathHelper.floor(pos.getZ())), instrument.getSound().value(), SoundCategory.RECORDS, 3.0F, f);
        this.world.addParticle(ParticleTypes.NOTE, pos.getX(), pos.getY() + 1.2, pos.getZ(), i / 24.0, 0.0, 0.0);
    }
}
