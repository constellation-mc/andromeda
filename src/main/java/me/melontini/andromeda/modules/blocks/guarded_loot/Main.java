package me.melontini.andromeda.modules.blocks.guarded_loot;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.items.lockpick.Lockpick;
import me.melontini.andromeda.modules.items.lockpick.LockpickItem;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Main {
    Main() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (player.getAbilities().creativeMode) return true;

            if (blockEntity instanceof LootableContainerBlockEntity && world.am$get(GuardedLoot.class).breakingHandler == GuardedLoot.BreakingHandler.UNBREAKABLE) {
                var monsters = checkMonsterLock(world, pos);
                if (monsters.isEmpty() || checkLockPicking(player)) return true;
                handleLockedContainer(player, monsters);
                return false;
            }
            return true;
        });
    }

    //TODO fix igloos. Maybe check reach?
    public static List<LivingEntity> checkMonsterLock(World world, BlockPos pos) {
        var config = world.am$get(GuardedLoot.class);
        if (!config.enabled) return Collections.emptyList();

        return world.getEntitiesByClass(LivingEntity.class, new Box(pos).expand(config.range), Entity::isAlive).stream()
                .filter(livingEntity -> livingEntity instanceof Monster)
                .toList();
    }

    public static boolean checkLockPicking(PlayerEntity player) {
        return ModuleManager.get().getModule(Lockpick.class).map(m -> {
            if (player.world.am$get(GuardedLoot.class).allowLockPicking) {
                if (player.getMainHandStack().isOf(LockpickItem.INSTANCE.orThrow())) {
                    return LockpickItem.INSTANCE.orThrow().tryUse(m, player.getMainHandStack(), player, Hand.MAIN_HAND);
                }
            }
            return false;
        }).orElse(false);
    }

    public static void handleLockedContainer(PlayerEntity player, Collection<LivingEntity> monsters) {
        player.sendMessage(TextUtil.translatable("andromeda.container.guarded").formatted(Formatting.RED), true);
        player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        player.emitGameEvent(GameEvent.CONTAINER_OPEN);

        for (LivingEntity livingEntity : monsters) {
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 5 * 20, 0, false, false));
        }
    }
}
