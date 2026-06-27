package dev.zenfyr.andromeda.modules.entities.minecarts.entities;

import dev.zenfyr.andromeda.modules.entities.boats.packets.RecordPlaybackS2CPayload;
import dev.zenfyr.andromeda.modules.entities.minecarts.MinecartEntities;
import dev.zenfyr.andromeda.modules.entities.minecarts.MinecartItems;
import dev.zenfyr.pulsar.api.itemstack.ItemStackUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class JukeboxMinecartEntity extends AbstractMinecart implements Clearable {

  public ItemStack record = ItemStack.EMPTY;

  public JukeboxMinecartEntity(
      EntityType<? extends JukeboxMinecartEntity> entityType, Level world) {
    super(entityType, world);
  }

  public JukeboxMinecartEntity(Level world, double x, double y, double z) {
    super(MinecartEntities.JUKEBOX_MINECART_ENTITY.orThrow(), world, x, y, z);
  }

  @Override
  public void activateMinecart(ServerLevel level, int x, int y, int z, boolean powered) {
    if (powered && !this.record.isEmpty()) {
      ItemStackUtil.spawnVelocity(
          new Vec3(this.getX(), this.getY() + 0.5, this.getZ()),
          this.record,
          this.level,
          -0.2,
          0.2,
          0.1,
          0.2,
          -0.2,
          0.2);
      this.clearContent();
      this.stopPlaying();
    }
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
    if (this.isRemoved()) {
      return true;
    } else if (this.isInvulnerableToBase(source)) {
      return false;
    } else {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.markHurt();
      this.setDamage(this.getDamage() + amount * 10.0F);
      this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
      boolean isCreativePlayer =
          source.getEntity() instanceof Player player && player.getAbilities().instabuild;
      if (isCreativePlayer || this.getDamage() > 40.0F) {
        this.ejectPassengers();
        this.stopPlaying();
        if (isCreativePlayer && !this.hasCustomName()) {
          this.discard();
        } else {
          this.destroy(level, source);
        }
      }

      return true;
    }
  }

  @Override
  public void destroy(ServerLevel level, DamageSource damageSource) {
    super.destroy(level, damageSource);
    if (level.getGameRules().get(GameRules.ENTITY_DROPS)) {
      this.spawnAtLocation(level, record.getItem());
    }
  }

  @Override
  public void kill(ServerLevel level) {
    this.stopPlaying();
    this.remove(RemovalReason.KILLED);
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    ItemStack stackInHand = player.getItemInHand(hand);
    if (!level.isClientSide())
      if (!this.record.isEmpty()) {
        ItemStackUtil.spawnVelocity(
            new Vec3(this.getX(), this.getY() + 0.5, this.getZ()),
            this.record,
            this.level,
            -0.2,
            0.2,
            0.1,
            0.2,
            -0.2,
            0.2);
        this.stopPlaying();
        this.clearContent();
      } else if (stackInHand.has(DataComponents.JUKEBOX_PLAYABLE)) {
        this.record = stackInHand.copy();
        this.startPlaying();
        stackInHand.shrink(1);
        player.awardStat(Stats.PLAY_RECORD);
      }
    return InteractionResult.SUCCESS;
  }

  public void stopPlaying() {
    for (Player player1 : level.players()) {
      ServerPlayNetworking.send(
          (ServerPlayer) player1, new RecordPlaybackS2CPayload(this.getUUID(), ItemStack.EMPTY));
    }
  }

  public void startPlaying() {
    for (Player player1 : level.players()) {
      ServerPlayNetworking.send(
          (ServerPlayer) player1, new RecordPlaybackS2CPayload(this.getUUID(), this.record));
    }
  }

  @Override
  public void readAdditionalSaveData(ValueInput nbt) {
    super.readAdditionalSaveData(nbt);
    if (nbt.contains("Items")) {
      this.record = nbt.read("Items", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
  }

  @Override
  public void addAdditionalSaveData(ValueOutput nbt) {
    super.addAdditionalSaveData(nbt);
    if (!this.record.isEmpty()) nbt.storeNullable("Items", ItemStack.OPTIONAL_CODEC, this.record);
  }

  @Override
  public Item getDropItem() {
    return MinecartItems.JUKEBOX_MINECART.orThrow();
  }

  @Override
  public BlockState getDefaultDisplayBlockState() {
    return Blocks.JUKEBOX.defaultBlockState();
  }

  @Override
  public ItemStack getPickResult() {
    return new ItemStack(MinecartItems.JUKEBOX_MINECART.orThrow());
  }

  @Override
  public void clearContent() {
    this.record = ItemStack.EMPTY;
  }
}
