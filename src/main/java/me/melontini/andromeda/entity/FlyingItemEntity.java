package me.melontini.andromeda.entity;

import me.melontini.andromeda.content.throwable_items.ItemBehavior;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.registries.EntityTypeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
        super(EntityTypeRegistry.FLYING_ITEM, d, e, f, world);
        this.setItem(stack);
    }

    public FlyingItemEntity(ItemStack stack, LivingEntity livingEntity, World world) {
        super(EntityTypeRegistry.FLYING_ITEM, livingEntity, world);
        this.setItem(stack);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        for (ItemBehavior itemBehavior : ItemBehaviorManager.getBehaviors(getItem().getItem())) {
            if (!this.isRemoved()) itemBehavior.onCollision(getItem(), this, world, getOwner(), hitResult);
        }
        this.discard();
    }

    @Override
    protected Item getDefaultItem() {
        return getItem().getItem();
    }
}
