package me.melontini.andromeda.modules.entities.boats.entities;

import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.modules.entities.boats.BoatEntities;
import me.melontini.andromeda.modules.entities.boats.BoatItems;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.Item;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.melontini.andromeda.modules.entities.boats.entities.BoatEntityWithBlock.PIby180;
import static me.melontini.andromeda.modules.entities.boats.entities.BoatEntityWithBlock.PIby2;

public class HopperBoatEntity extends ChestBoatEntity implements Hopper {
    private final BlockPos currentBlockPos = BlockPos.ORIGIN;
    public int transferCooldown = -1;

    public HopperBoatEntity(EntityType<? extends BoatEntity> entityType, World world) {
        super(entityType, world);
        this.resetInventory();
    }

    public HopperBoatEntity(World world, double x, double y, double z) {
        this(BoatEntities.BOAT_WITH_HOPPER.orThrow(), world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new HopperScreenHandler(i, playerInventory, this);
    }

    @Override
    public int size() {
        return 5;
    }

    @Override
    public double getHopperX() {
        Vec3d vec3d = new Vec3d(-0.8, 0.0, 0.0).rotateY(-this.getYaw() * PIby180 - PIby2);
        return this.getX() + vec3d.x;
    }

    @Override
    public double getHopperY() {
        return this.getY() + 0.5;
    }

    @Override
    public double getHopperZ() {
        Vec3d vec3d = new Vec3d(-0.8, 0.0, 0.0).rotateY(-this.getYaw() * PIby180 - PIby2);
        return this.getZ() + vec3d.z;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.isClient && this.isAlive()) {
            BlockPos blockPos = this.getBlockPos();
            if (blockPos.equals(this.currentBlockPos)) {
                --this.transferCooldown;
            } else {
                this.setTransferCooldown(0);
            }

            if (!this.isCoolingDown()) {
                this.setTransferCooldown(0);
                if (this.canOperate()) {
                    this.setTransferCooldown(4);
                    this.markDirty();
                }
            }
        }

    }

    @Override
    public Item asItem() {
        return CommonRegistries.items().get(BoatItems.boatId(this.getBoatType(), "hopper"));
    }

    public boolean canOperate() {
        if (HopperBlockEntity.extract(this.world, this)) {
            return true;
        } else {
            List<ItemEntity> list = this.world.getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.25, 0.0, 0.25), EntityPredicates.VALID_ENTITY);
            if (!list.isEmpty()) {
                HopperBlockEntity.extract(this, list.get(0));
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
