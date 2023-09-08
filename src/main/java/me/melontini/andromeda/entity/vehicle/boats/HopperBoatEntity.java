package me.melontini.andromeda.entity.vehicle.boats;

import me.melontini.andromeda.registries.EntityTypeRegistry;
import me.melontini.andromeda.registries.ItemRegistry;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class HopperBoatEntity extends StorageBoatEntity implements Hopper {
    private final BlockPos currentBlockPos = BlockPos.ORIGIN;
    public int transferCooldown = -1;

    public HopperBoatEntity(EntityType<? extends BoatEntity> entityType, World world) {
        super(entityType, world);
    }

    public HopperBoatEntity(World world, double x, double y, double z) {
        this(EntityTypeRegistry.get().BOAT_WITH_HOPPER, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Override
    public ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new HopperScreenHandler(syncId, playerInventory, this);
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
        return Registries.ITEM.get(ItemRegistry.boatId(this.getVariant(), "hopper"));
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
