package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

public record Context(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult, LootContext lootContext) {

}
