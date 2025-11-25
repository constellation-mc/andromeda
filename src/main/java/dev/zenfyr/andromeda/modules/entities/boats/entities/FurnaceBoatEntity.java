package dev.zenfyr.andromeda.modules.entities.boats.entities;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.better_furnace_minecart.BetterFurnaceMinecart;
import dev.zenfyr.andromeda.modules.entities.boats.BoatEntities;
import dev.zenfyr.andromeda.modules.entities.boats.BoatItems;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FurnaceBoatEntity extends BoatEntityWithBlock {
  private static final EntityDataAccessor<Integer> FUEL =
      SynchedEntityData.defineId(FurnaceBoatEntity.class, EntityDataSerializers.INT);

  public FurnaceBoatEntity(EntityType<? extends Boat> entityType, Level world) {
    super(entityType, world);
  }

  public FurnaceBoatEntity(Level world, double x, double y, double z) {
    this(BoatEntities.BOAT_WITH_FURNACE.orThrow(), world);
    this.setPos(x, y, z);
    this.xo = x;
    this.yo = y;
    this.zo = z;
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(FUEL, 0);
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
    if (FuelRegistry.INSTANCE.get(stack.getItem()) != null) {
      int itemFuel = FuelRegistry.INSTANCE.get(stack.getItem());
      if ((this.getFuel() + (itemFuel * 2.25))
          <= ModuleManager.get()
              .get(BetterFurnaceMinecart.class)
              .map(m -> Andromeda.MAIN.get(BetterFurnaceMinecart.CONFIG).maxFuel)
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
  public void addAdditionalSaveData(CompoundTag nbt) {
    super.addAdditionalSaveData(nbt);
    nbt.putInt("AM-Fuel", this.getFuel());
  }

  @Override
  public void readAdditionalSaveData(CompoundTag nbt) {
    super.readAdditionalSaveData(nbt);
    setFuel(nbt.getInt("AM-Fuel"));
  }

  @Override
  public Item getDropItem() {
    return BuiltInRegistries.ITEM.get(BoatItems.boatId(this.getVariant(), "furnace"));
  }

  public int getFuel() {
    return this.entityData.get(FUEL);
  }

  public void setFuel(int fuel) {
    this.entityData.set(FUEL, fuel);
  }
}
