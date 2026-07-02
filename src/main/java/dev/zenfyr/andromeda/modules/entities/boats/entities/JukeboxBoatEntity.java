package dev.zenfyr.andromeda.modules.entities.boats.entities;

import dev.zenfyr.andromeda.modules.entities.boats.BoatEntities;
import dev.zenfyr.andromeda.modules.entities.boats.BoatItems;
import dev.zenfyr.andromeda.modules.entities.boats.client.ClientSoundHolder;
import dev.zenfyr.pulsar.api.itemstack.ItemStackUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class JukeboxBoatEntity extends BoatEntityWithBlock implements Clearable {

  public ItemStack record = ItemStack.EMPTY;

  public JukeboxBoatEntity(EntityType<? extends Boat> entityType, Level world) {
    super(entityType, world);
  }

  public JukeboxBoatEntity(Level world, double x, double y, double z) {
    this(BoatEntities.BOAT_WITH_JUKEBOX.orThrow(), world);
    this.setPos(x, y, z);
    this.xo = x;
    this.yo = y;
    this.zo = z;
  }

  @Override
  public boolean hurt(DamageSource source, float amount) {
    if (this.isInvulnerableTo(source)) {
      return false;
    } else if (!this.level.isClientSide() && !this.isRemoved()) {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() + amount * 10.0F);
      this.markHurt();
      this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
      boolean bl = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
      if (bl || this.getDamage() > 40.0F) {
        this.stopPlaying();
        if (!bl && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
          this.spawnAtLocation(this.getDropItem());
        }

        this.discard();
      }

      return true;
    } else {
      return true;
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
      } else if (stackInHand.getItem() instanceof RecordItem && record.isEmpty()) {
        this.record = stackInHand.copy();
        this.startPlaying();
        stackInHand.shrink(1);
        player.awardStat(Stats.PLAY_RECORD);
        return InteractionResult.SUCCESS;
      }
    super.interact(player, hand);
    return InteractionResult.sidedSuccess(this.level.isClientSide());
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
  public Item getDropItem() {
    return BuiltInRegistries.ITEM.get(BoatItems.boatId(this.getVariant(), "jukebox"));
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
  public void clearContent() {
    this.record = ItemStack.EMPTY;
  }
}
