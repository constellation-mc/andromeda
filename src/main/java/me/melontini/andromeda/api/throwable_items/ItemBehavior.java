package me.melontini.andromeda.api.throwable_items;

import me.melontini.andromeda.entity.FlyingItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemBehavior {

    void onCollision(ItemStack stack, FlyingItemEntity flyingItemEntity, World world, @Nullable Entity user, HitResult hitResult);

}
