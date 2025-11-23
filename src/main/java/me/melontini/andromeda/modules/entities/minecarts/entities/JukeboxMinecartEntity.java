package me.melontini.andromeda.modules.entities.minecarts.entities;

import me.melontini.andromeda.modules.entities.boats.client.ClientSoundHolder;
import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.MinecartItems;
import me.melontini.dark_matter.api.minecraft.util.ItemStackUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

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
  public Type getMinecartType() {
    return Type.CHEST;
  }

  @Override
  public void activateMinecart(int x, int y, int z, boolean powered) {
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
  public boolean hurt(DamageSource source, float amount) {
    if (this.level.isClientSide || this.isRemoved()) {
      return true;
    } else if (this.isInvulnerableTo(source)) {
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
          this.destroy(source);
        }
      }

      return true;
    }
  }

  @Override
  public void destroy(DamageSource damageSource) {
    super.destroy(damageSource);
    if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
      this.spawnAtLocation(record.getItem());
    }
  }

  @Override
  public void kill() {
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
      } else if (stackInHand.getItem() instanceof RecordItem) {
        this.record = stackInHand.copy();
        this.startPlaying();
        stackInHand.shrink(1);
        player.awardStat(Stats.PLAY_RECORD);
      }
    return InteractionResult.sidedSuccess(this.level.isClientSide);
  }

  public void stopPlaying() {
    FriendlyByteBuf buf = PacketByteBufs.create().writeUUID(this.getUUID());

    for (Player player1 : level.players()) {
      ServerPlayNetworking.send(
          (ServerPlayer) player1, ClientSoundHolder.JUKEBOX_STOP_PLAYING, buf);
    }
  }

  public void startPlaying() {
    FriendlyByteBuf buf = PacketByteBufs.create().writeUUID(this.uuid).writeItem(this.record);

    for (Player player1 : level.players()) {
      ServerPlayNetworking.send(
          (ServerPlayer) player1, ClientSoundHolder.JUKEBOX_START_PLAYING, buf);
    }
  }

  @Override
  public void readAdditionalSaveData(CompoundTag nbt) {
    super.readAdditionalSaveData(nbt);
    if (nbt.contains("Items", 10)) {
      this.record = ItemStack.of(nbt.getCompound("Items"));
    }
  }

  @Override
  public void addAdditionalSaveData(CompoundTag nbt) {
    super.addAdditionalSaveData(nbt);
    if (!this.record.isEmpty()) nbt.put("Items", this.record.save(new CompoundTag()));
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
