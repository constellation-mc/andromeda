package dev.zenfyr.andromeda.modules.entities.boats.entities;

import dev.zenfyr.andromeda.modules.entities.boats.packets.RecordPlaybackS2CPayload;
import dev.zenfyr.pulsar.api.itemstack.ItemStackUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class JukeboxBoatEntity extends BoatEntityWithBlock implements Clearable {

  public ItemStack record = ItemStack.EMPTY;

  public JukeboxBoatEntity(
      EntityType<? extends AbstractBoat> entityType,
      Level world,
      BoatRideHeightFactory rideHeight,
      Supplier<Item> dropItem) {
    super(entityType, world, rideHeight, dropItem);
  }

  @Override
  protected Component getTypeName() {
    return TextUtil.translatable("entity.andromeda.jukebox_boat");
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
    if (this.isInvulnerableToBase(source)) {
      return false;
    } else if (!this.isRemoved()) {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() + amount * 10.0F);
      this.markHurt();
      this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
      boolean bl = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
      if (bl || this.getDamage() > 40.0F) {
        this.stopPlaying();
        if (!bl && level.getGameRules().get(GameRules.ENTITY_DROPS)) {
          this.spawnAtLocation(level, this.getDropItem());
        }

        this.discard();
      }

      return true;
    } else {
      return true;
    }
  }

  @Override
  public void kill(ServerLevel level) {
    this.stopPlaying();
    this.remove(RemovalReason.KILLED);
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
    ItemStack stackInHand = player.getItemInHand(hand);
    if (!level.isClientSide())
      if (!this.record.isEmpty() && player.isShiftKeyDown()) {
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
        return InteractionResult.SUCCESS;
      } else if (stackInHand.has(DataComponents.JUKEBOX_PLAYABLE)) {
        this.record = stackInHand.copy();
        this.startPlaying();
        stackInHand.shrink(1);
        player.awardStat(Stats.PLAY_RECORD);
        return InteractionResult.SUCCESS;
      }
    super.interact(player, hand, location);
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
  public void clearContent() {
    this.record = ItemStack.EMPTY;
  }
}
