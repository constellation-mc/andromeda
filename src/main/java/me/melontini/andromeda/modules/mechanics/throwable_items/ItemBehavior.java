package me.melontini.andromeda.modules.mechanics.throwable_items;

import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemBehavior {

  void onCollision(
          ItemStack stack,
          FlyingItemEntity fie,
          ServerLevel world,
          @Nullable Entity user,
          HitResult hitResult);

  static int getCooldown(
          ServerLevel world, @Nullable Entity user, FlyingItemEntity fie, ItemStack stack) {
    var cd = world.getServer().dm$getReloader(RELOADER).getCooldown(stack.getItem());
    if (cd.toSource().left().isPresent()) return cd.asInt(null); // constant, can pass null.

    LootParams.Builder builder = new LootParams.Builder(world);
    builder.withParameter(LootContextParams.DIRECT_KILLER_ENTITY, fie);
    builder.withOptionalParameter(LootContextParams.KILLER_ENTITY, user);
    builder.withParameter(LootContextParams.TOOL, stack);
    builder.withParameter(LootContextParams.ORIGIN, fie.position());

    LootContext lootContext =
        new LootContext.Builder(builder.create(Main.CONTEXT_TYPE.orThrow())).create(null);
    return cd.asInt(lootContext);
  }
}
