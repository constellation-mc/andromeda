package me.melontini.andromeda.modules.blocks.guarded_loot;

import static me.melontini.andromeda.api.ModuleDeclarations.LOOT_UNLOCKER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public final class Main {

  private static final List<BiPredicate<BlockEntity, Player>> UNLOCKERS = new ArrayList<>();

  static void init() {
    for (var listener : ModuleManager.get().getModuleApiListeners(LOOT_UNLOCKER)) {
      listener.accept(lootUnlocker -> {
        UNLOCKERS.add(lootUnlocker);
        return null;
      });
    }

    PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
      if (player.getAbilities().instabuild) return true;

      if (blockEntity instanceof RandomizableContainerBlockEntity
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
      Level world, BlockState state, Player player, BlockPos pos, BlockEntity be) {
    var config = world.am$get(GuardedLoot.CONFIG);
    if (!config.available) return Collections.emptyList();

    return world
        .getEntitiesOfClass(
            LivingEntity.class, new AABB(pos).inflate(config.range), Entity::isAlive)
        .stream()
        .filter(Enemy.class::isInstance)
        .toList();
  }

  public static boolean checkLockPicking(BlockEntity entity, Player player) {
    if (UNLOCKERS.isEmpty()) return false;

    for (BiPredicate<BlockEntity, Player> unlocker : UNLOCKERS) {
      if (unlocker.test(entity, player)) return true;
    }
    return false;
  }

  public static void handleLockedContainer(Player player, Collection<LivingEntity> monsters) {
    player.displayClientMessage(
        TextUtil.translatable("andromeda.container.guarded").withStyle(ChatFormatting.RED), true);
    player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
    player.gameEvent(GameEvent.CONTAINER_OPEN);

    for (LivingEntity livingEntity : monsters) {
      livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5 * 20, 0, false, false));
    }
  }
}
