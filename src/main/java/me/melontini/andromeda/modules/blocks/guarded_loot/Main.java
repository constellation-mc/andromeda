package me.melontini.andromeda.modules.blocks.guarded_loot;

import static me.melontini.andromeda.api.ModuleDeclarations.LOOT_UNLOCKER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public final class Main {

  private static final List<BiPredicate<BlockEntity, PlayerEntity>> UNLOCKERS = new ArrayList<>();

  static void init() {
    for (var listener : ModuleManager.get().getModuleApiListeners(LOOT_UNLOCKER)) {
      listener.accept(lootUnlocker -> {
        UNLOCKERS.add(lootUnlocker);
        return null;
      });
    }

    PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
      if (player.getAbilities().creativeMode) return true;

      if (blockEntity instanceof LootableContainerBlockEntity
          && world.am$get(GuardedLoot.CONFIG).breakingHandler
              == GuardedLoot.BreakingHandler.UNBREAKABLE) {
        var monsters = checkMonsterLock(world, state, player, pos, blockEntity);
        if (monsters.isEmpty() || checkLockPicking(blockEntity, player)) return true;
        handleLockedContainer(player, monsters);
        return false;
      }
      return true;
    });
  }

  // TODO fix igloos. Maybe check reach?
  public static List<LivingEntity> checkMonsterLock(
      World world, BlockState state, PlayerEntity player, BlockPos pos, BlockEntity be) {
    var config = world.am$get(GuardedLoot.CONFIG);
    var supplier = LootContextBuilder.block(
        world, builder -> builder.origin(pos).state(state).thisEntity(player).blockEntity(be));
    if (!config.available.asBoolean(supplier)) return Collections.emptyList();

    return world
        .getEntitiesByClass(
            LivingEntity.class,
            new Box(pos).expand(config.range.asDouble(supplier)),
            Entity::isAlive)
        .stream()
        .filter(Monster.class::isInstance)
        .toList();
  }

  public static boolean checkLockPicking(BlockEntity entity, PlayerEntity player) {
    if (UNLOCKERS.isEmpty()) return false;

    for (BiPredicate<BlockEntity, PlayerEntity> unlocker : UNLOCKERS) {
      if (unlocker.test(entity, player)) return true;
    }
    return false;
  }

  public static void handleLockedContainer(PlayerEntity player, Collection<LivingEntity> monsters) {
    player.sendMessage(
        TextUtil.translatable("andromeda.container.guarded").formatted(Formatting.RED), true);
    player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
    player.emitGameEvent(GameEvent.CONTAINER_OPEN);

    for (LivingEntity livingEntity : monsters) {
      livingEntity.addStatusEffect(
          new StatusEffectInstance(StatusEffects.GLOWING, 5 * 20, 0, false, false));
    }
  }
}
