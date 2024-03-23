package me.melontini.andromeda.modules.mechanics.throwable_items;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemBehavior {

    void onCollision(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult);

}
