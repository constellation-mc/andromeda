package me.melontini.andromeda.modules.entities.minecarts.entities;

import me.melontini.andromeda.modules.entities.boats.packets.StartPayload;
import me.melontini.andromeda.modules.entities.boats.packets.StopPayload;
import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.MinecartItems;
import me.melontini.dark_matter.api.minecraft.util.ItemStackUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class JukeboxMinecartEntity extends AbstractMinecartEntity implements Clearable {

  public ItemStack record = ItemStack.EMPTY;

  public JukeboxMinecartEntity(
      EntityType<? extends JukeboxMinecartEntity> entityType, World world) {
    super(entityType, world);
  }

  public JukeboxMinecartEntity(World world, double x, double y, double z) {
    super(MinecartEntities.JUKEBOX_MINECART_ENTITY.orThrow(), world, x, y, z);
  }

  @Override
  public Type getMinecartType() {
    return Type.CHEST;
  }

  @Override
  public void onActivatorRail(int x, int y, int z, boolean powered) {
    if (powered && !this.record.isEmpty()) {
      ItemStackUtil.spawnVelocity(
          new Vec3d(this.getX(), this.getY() + 0.5, this.getZ()),
          this.record,
          this.world,
          -0.2,
          0.2,
          0.1,
          0.2,
          -0.2,
          0.2);
      this.clear();
      this.stopPlaying();
    }
  }

  @Override
  public boolean damage(DamageSource source, float amount) {
    if (this.world.isClient || this.isRemoved()) {
      return true;
    } else if (this.isInvulnerableTo(source)) {
      return false;
    } else {
      this.setDamageWobbleSide(-this.getDamageWobbleSide());
      this.setDamageWobbleTicks(10);
      this.scheduleVelocityUpdate();
      this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0F);
      this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
      boolean isCreativePlayer =
          source.getAttacker() instanceof PlayerEntity player && player.getAbilities().creativeMode;
      if (isCreativePlayer || this.getDamageWobbleStrength() > 40.0F) {
        this.removeAllPassengers();
        this.stopPlaying();
        if (isCreativePlayer && !this.hasCustomName()) {
          this.discard();
        } else {
          this.killAndDropSelf(source);
        }
      }

      return true;
    }
  }

  @Override
  public void killAndDropSelf(DamageSource damageSource) {
    super.killAndDropSelf(damageSource);
    if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
      this.dropItem(record.getItem());
    }
  }

  @Override
  public void kill() {
    this.stopPlaying();
    this.remove(Entity.RemovalReason.KILLED);
  }

  @Override
  public ActionResult interact(PlayerEntity player, Hand hand) {
    ItemStack stackInHand = player.getStackInHand(hand);
    if (!world.isClient())
      if (!this.record.isEmpty()) {
        ItemStackUtil.spawnVelocity(
            new Vec3d(this.getX(), this.getY() + 0.5, this.getZ()),
            this.record,
            this.world,
            -0.2,
            0.2,
            0.1,
            0.2,
            -0.2,
            0.2);
        this.stopPlaying();
        this.clear();
      } else if (stackInHand.contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
        this.record = stackInHand.copy();
        this.startPlaying();
        stackInHand.decrement(1);
        player.incrementStat(Stats.PLAY_RECORD);
      }
    return ActionResult.success(this.world.isClient);
  }

  public void stopPlaying() {
    var payload = new StopPayload(this.getUuid());
        for (PlayerEntity player1 : world.getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity) player1, payload);
    }
  }

  public void startPlaying() {
    var payload = new StartPayload(this.getUuid(), this.record);
        for (PlayerEntity player1 : world.getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity) player1, payload);
    }
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains("Items", 10)) {
      this.record = ItemStack.fromNbtOrEmpty(this.getRegistryManager(), nbt.getCompound("Items"));
    }
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    if (!this.record.isEmpty()) nbt.put("Items", this.record.encode(this.getRegistryManager()));
  }

  @Override
  public Item asItem() {
    return MinecartItems.JUKEBOX_MINECART.orThrow();
  }

  @Override
  public BlockState getDefaultContainedBlock() {
    return Blocks.JUKEBOX.getDefaultState();
  }

  @Override
  public ItemStack getPickBlockStack() {
    return new ItemStack(MinecartItems.JUKEBOX_MINECART.orThrow());
  }

  @Override
  public void clear() {
    this.record = ItemStack.EMPTY;
  }
}
