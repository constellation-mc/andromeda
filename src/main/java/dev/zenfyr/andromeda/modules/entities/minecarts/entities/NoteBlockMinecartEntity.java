package dev.zenfyr.andromeda.modules.entities.minecarts.entities;

import dev.zenfyr.andromeda.modules.entities.minecarts.MinecartEntities;
import dev.zenfyr.andromeda.modules.entities.minecarts.MinecartItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class NoteBlockMinecartEntity extends AbstractMinecart {
  public int note = 0;
  public boolean isPowered = false;

  public NoteBlockMinecartEntity(
      EntityType<? extends NoteBlockMinecartEntity> entityType, Level world) {
    super(entityType, world);
  }

  public NoteBlockMinecartEntity(Level world, double x, double y, double z) {
    super(MinecartEntities.NOTEBLOCK_MINECART_ENTITY.orThrow(), world, x, y, z);
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
    this.playNote(level(), new Vec3(getX(), getY() - 1, getZ()));
    super.hurtServer(level, source, amount);
    return true;
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
    this.cycleNote();
    this.playNote(level(), new Vec3(getX(), getY(), getZ()));
    player.awardStat(Stats.TUNE_NOTEBLOCK);
    return InteractionResult.SUCCESS;
  }

  @Override
  public void activateMinecart(ServerLevel level, int x, int y, int z, boolean powered) {
    if (powered && !this.isPowered) {
      playNote(this.level(), new Vec3(getX(), getY(), getZ()));
    }
  }

  @Override
  public void tick() {
    int i = Mth.floor(this.getX());
    int j = Mth.floor(this.getY());
    int k = Mth.floor(this.getZ());
    if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
      --j;
    }

    if (!level().isClientSide()) {
      BlockPos blockPos = new BlockPos(i, j, k);
      BlockState blockState = this.level().getBlockState(blockPos);
      if (BaseRailBlock.isRail(blockState)) {
        if (blockState.is(Blocks.ACTIVATOR_RAIL)) {
          if (blockState.getValue(PoweredRailBlock.POWERED)) {
            this.activateMinecart((ServerLevel) level(), i, j, k, true);
            this.isPowered = true;
          } else {
            this.isPowered = false;
          }
        } else {
          this.isPowered = false;
        }
      }
    }
    super.tick();
  }

  @Override
  public void readAdditionalSaveData(ValueInput nbt) {
    super.readAdditionalSaveData(nbt);
    this.note = nbt.getIntOr("Note", 0);
    this.isPowered = nbt.getBooleanOr("Powered", false);
  }

  @Override
  public void addAdditionalSaveData(ValueOutput nbt) {
    super.addAdditionalSaveData(nbt);
    nbt.putInt("Note", this.note);
    nbt.putBoolean("Powered", this.isPowered);
  }

  @Override
  public Item getDropItem() {
    return MinecartItems.NOTE_BLOCK_MINECART.orThrow();
  }

  @Override
  public BlockState getDefaultDisplayBlockState() {
    return Blocks.NOTE_BLOCK.defaultBlockState();
  }

  @Override
  public ItemStack getPickResult() {
    return new ItemStack(MinecartItems.NOTE_BLOCK_MINECART.orThrow());
  }

  public void cycleNote() {
    int nextNote = this.note + 1;
    if (nextNote < BlockStateProperties.NOTE.getAllValues().toList().size()) {
      this.note = nextNote;
    } else {
      this.note = 0;
    }
  }

  public void playNote(Level world, Vec3 pos) {
    BlockPos blockPos = new BlockPos(Mth.floor(pos.x()), Mth.floor(pos.y()), Mth.floor(pos.z()));
    // BlockState state = world.getBlockState(blockPos);

    NoteBlockInstrument instrument = world.getBlockState(blockPos.above()).instrument();
    if (!instrument.worksAboveNoteBlock()) {
      NoteBlockInstrument instrument2 = world.getBlockState(blockPos.below()).instrument();
      instrument = instrument2.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : instrument2;
    }

    int i = this.note;
    float f = (float) Math.pow(2.0, (i - 12) / 12.0);
    this.level()
        .playSound(
            null,
            new BlockPos(Mth.floor(pos.x()), Mth.floor(pos.y()), Mth.floor(pos.z())),
            instrument.getSoundEvent().value(),
            SoundSource.RECORDS,
            3.0F,
            f);
    this.level()
        .addParticle(ParticleTypes.NOTE, pos.x(), pos.y() + 1.2, pos.z(), i / 24.0, 0.0, 0.0);
  }
}
