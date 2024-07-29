package me.melontini.andromeda.modules.mechanics.throwable_items;

import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemBehavior {

  void onCollision(
      ItemStack stack,
      FlyingItemEntity fie,
      ServerWorld world,
      @Nullable Entity user,
      HitResult hitResult);

  static int getCooldown(
      ServerWorld world, @Nullable Entity user, FlyingItemEntity fie, ItemStack stack) {
    var cd = world.getServer().dm$getReloader(RELOADER).getCooldown(stack.getItem());
    if (cd.toSource().left().isPresent()) return cd.asInt(null); // constant, can pass null.

    LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world);
    builder.add(LootContextParameters.ATTACKING_ENTITY, fie);
    builder.addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, user);
    builder.add(LootContextParameters.TOOL, stack);
    builder.add(LootContextParameters.ORIGIN, fie.getPos());

    LootContext lootContext =
        new LootContext.Builder(builder.build(Main.CONTEXT_TYPE.orThrow())).build(Optional.empty());
    return cd.asInt(lootContext);
  }
}
