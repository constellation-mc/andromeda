package dev.zenfyr.andromeda.modules.entities.boats.entities;

import static dev.zenfyr.andromeda.modules.entities.boats.entities.BoatEntityWithBlock.PIby180;
import static dev.zenfyr.andromeda.modules.entities.boats.entities.BoatEntityWithBlock.PIby2;

import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HopperBoatEntity extends AbstractChestBoat implements Hopper {
  private final BlockPos currentBlockPos = BlockPos.ZERO;
  public int transferCooldown = -1;

  private final BoatRideHeightFactory rideHeight;

  public HopperBoatEntity(
      EntityType<? extends AbstractChestBoat> entityType,
      Level world,
      BoatRideHeightFactory rideHeight,
      Supplier<Item> dropItem) {
    super(entityType, world, dropItem);
    this.clearItemStacks();
    this.rideHeight = rideHeight;
  }

  @Override
  protected Component getTypeName() {
    return TextUtil.translatable("entity.andromeda.hopper_boat");
  }

  @Override
  protected double rideHeight(EntityDimensions dimensions) {
    return this.rideHeight.applyAsDouble(dimensions);
  }

  @Nullable @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new HopperMenu(i, playerInventory, this);
  }

  @Override
  public int getContainerSize() {
    return 5;
  }

  @Override
  public double getLevelX() {
    Vec3 vec3d = new Vec3(-0.8, 0.0, 0.0).yRot(-this.getYRot() * PIby180 - PIby2);
    return this.getX() + vec3d.x;
  }

  @Override
  public double getLevelY() {
    return this.getY() + 0.5;
  }

  @Override
  public double getLevelZ() {
    Vec3 vec3d = new Vec3(-0.8, 0.0, 0.0).yRot(-this.getYRot() * PIby180 - PIby2);
    return this.getZ() + vec3d.z;
  }

  @Override
  public boolean isGridAligned() {
    return false;
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.level.isClientSide() && this.isAlive()) {
      BlockPos blockPos = this.blockPosition();
      if (blockPos.equals(this.currentBlockPos)) {
        --this.transferCooldown;
      } else {
        this.setTransferCooldown(0);
      }

      if (!this.isCoolingDown()) {
        this.setTransferCooldown(0);
        if (this.canOperate()) {
          this.setTransferCooldown(4);
          this.setChanged();
        }
      }
    }
  }

  public boolean canOperate() {
    if (HopperBlockEntity.suckInItems(this.level, this)) {
      return true;
    } else {
      List<ItemEntity> list = this.level.getEntitiesOfClass(
          ItemEntity.class,
          this.getBoundingBox().inflate(0.25, 0.0, 0.25),
          EntitySelector.ENTITY_STILL_ALIVE);
      if (!list.isEmpty()) {
        HopperBlockEntity.addItem(this, list.get(0));
      }

      return false;
    }
  }

  public void setTransferCooldown(int transferCooldown) {
    this.transferCooldown = transferCooldown;
  }

  public boolean isCoolingDown() {
    return this.transferCooldown > 0;
  }
}
