package me.melontini.andromeda.modules.mechanics.throwable_items;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemBehavior {

    void onCollision(ItemStack stack, FlyingItemEntity flyingItemEntity, World world, @Nullable Entity user, HitResult hitResult);

}
