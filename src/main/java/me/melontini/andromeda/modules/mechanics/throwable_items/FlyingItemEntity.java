package me.melontini.andromeda.modules.mechanics.throwable_items;

import static java.util.Objects.requireNonNull;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class FlyingItemEntity extends ThrowableItemProjectile {

  public FlyingItemEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
    super(entityType, world);
    this.setItem(ItemStack.EMPTY);
  }

  public FlyingItemEntity(ItemStack stack, double d, double e, double f, Level world) {
    super(Main.FLYING_ITEM.orThrow(), d, e, f, world);
    this.setItem(stack);
  }

  public FlyingItemEntity(ItemStack stack, Entity entity, Level world) {
    super(Main.FLYING_ITEM.orThrow(), world);
    this.setOwner(entity);
    this.setItem(stack);
  }

  @Override
  protected void onHit(HitResult hitResult) {
    if (!this.level.isClientSide()) {
      for (ItemBehavior behavior : requireNonNull(this.level.getServer())
          .dm$getReloader(ItemBehaviorManager.RELOADER)
          .getBehaviors(getItemRaw().getItem())) {
        if (!this.isRemoved())
          behavior.onCollision(getItemRaw(), this, (ServerLevel) level, getOwner(), hitResult);
      }
    }
    this.discard();
  }

  @Override
  protected Item getDefaultItem() {
    return getItemRaw().getItem();
  }
}
