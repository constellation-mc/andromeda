package dev.zenfyr.andromeda.modules.entities.boats.entities;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.furnace_minecart_tweaks.FurnaceMinecartTweaks;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class FurnaceBoatEntity extends BoatEntityWithBlock {
  private static final EntityDataAccessor<Integer> FUEL =
      SynchedEntityData.defineId(FurnaceBoatEntity.class, EntityDataSerializers.INT);

  public FurnaceBoatEntity(
      EntityType<? extends AbstractBoat> entityType,
      Level world,
      BoatRideHeightFactory rideHeight,
      Supplier<Item> dropItem) {
    super(entityType, world, rideHeight, dropItem);
  }

  @Override
  protected Component getTypeName() {
    return TextUtil.translatable("entity.andromeda.furnace_boat");
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(FUEL, 0);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.getFuel() > 0) {
      this.setFuel(this.getFuel() - 1);
      if (this.level.random.nextInt(4) == 0) {
        Vec3 vec3d = new Vec3(-0.8, 0.0, 0.0).yRot(-this.getYRot() * PIby180 - PIby2);
        this.level.addParticle(
            ParticleTypes.CAMPFIRE_COSY_SMOKE,
            this.getX() + vec3d.x,
            this.getY() + 0.8,
            this.getZ() + vec3d.z,
            -(this.getDeltaMovement().x * 0.3),
            0.08,
            -(this.getDeltaMovement().z * 0.3));
      }
    }
  }

  @Override
  public void floatBoat() {
    super.floatBoat();
    Vec3 vec3d = this.getDeltaMovement();
    if (this.getFuel() > 0) {
      Vec3 rotationVec = this.getViewVector(1.0F);
      if (this.status == Status.ON_LAND)
        this.setDeltaMovement(rotationVec.x() * 0.1, vec3d.y, rotationVec.z() * 0.1);
      else this.setDeltaMovement(rotationVec.x() * 0.4, vec3d.y, rotationVec.z() * 0.4);
    }
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (level.fuelValues().isFuel(stack)) {
      int itemFuel = level.fuelValues().burnDuration(stack);
      if ((this.getFuel() + (itemFuel * 2.25))
          <= ModuleManager.get()
              .get(FurnaceMinecartTweaks.class)
              .map(m -> Andromeda.MAIN.get(FurnaceMinecartTweaks.CONFIG).maxFuel)
              .orElse(45000)) {
        if (!player.getAbilities().instabuild) {
          ItemStack reminder = stack.getRecipeRemainder();
          if (!reminder.isEmpty())
            player.getInventory().placeItemBackInInventory(stack.getRecipeRemainder());
          stack.shrink(1);
        }

        this.setFuel((int) (this.getFuel() + (itemFuel * 2.25)));
        return InteractionResult.SUCCESS;
      }
    }
    return super.interact(player, hand);
  }

  @Override
  public void addAdditionalSaveData(ValueOutput nbt) {
    super.addAdditionalSaveData(nbt);
    nbt.putInt("AM-Fuel", this.getFuel());
  }

  @Override
  public void readAdditionalSaveData(ValueInput nbt) {
    super.readAdditionalSaveData(nbt);
    setFuel(nbt.getIntOr("AM-Fuel", 0));
  }

  public int getFuel() {
    return this.entityData.get(FUEL);
  }

  public void setFuel(int fuel) {
    this.entityData.set(FUEL, fuel);
  }
}
