package me.melontini.andromeda.entity;

import me.melontini.andromeda.api.throwable_items.ItemBehavior;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.registries.EntityTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class FlyingItemEntity extends ThrownItemEntity {


    public FlyingItemEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
        this.setItem(ItemStack.EMPTY);
    }

    public FlyingItemEntity(ItemStack stack, double d, double e, double f, World world) {
        super(EntityTypeRegistry.get().FLYING_ITEM.get(), d, e, f, world);
        this.setItem(stack);
    }

    public FlyingItemEntity(ItemStack stack, Entity entity, World world) {
        super(EntityTypeRegistry.get().FLYING_ITEM.get(), world);
        this.setOwner(entity);
        this.setItem(stack);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        for (ItemBehavior behavior : ItemBehaviorManager.getBehaviors(getItem().getItem())) {
            if (!this.isRemoved()) behavior.onCollision(getItem(), this, world, getOwner(), hitResult);
        }
        this.discard();
    }

    @Override
    protected Item getDefaultItem() {
        return getItem().getItem();
    }
}
