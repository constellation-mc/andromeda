package dev.zenfyr.andromeda.modules.items.fire_damage;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.pulsar.api.util.MathUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FireDamageMain {

  public static final TagKey<Item> FIRE_DAMAGEABLE =
      TagKey.create(Registries.ITEM, Andromeda.id("fire_damageable"));

  public static void init() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamageTaken, damageTaken, blocked) -> {
          if (!source.is(DamageTypeTags.IS_FIRE)) return;
          if (!entity.level().am$get(FireDamage.CONFIG).available) return;

          if (blocked) {
            applyShield(entity);
            return;
          }

          if (entity instanceof Player player) {
            player.getInventory().forEach(FireDamageMain::damage);
            applyShield(player);
          } else if (entity instanceof InventoryCarrier carrier) {
            carrier.getInventory().forEach(FireDamageMain::damage);
          }
        });
  }

  private static void applyShield(LivingEntity entity) {
    for (InteractionHand value : InteractionHand.values()) {
      ItemStack itemStack = entity.getItemInHand(value);
      if (itemStack.is(ConventionalItemTags.SHIELD_TOOLS) && itemStack.is(FIRE_DAMAGEABLE)) {
        if (entity instanceof Player player) player.getCooldowns().addCooldown(itemStack, 10);
        entity.stopUsingItem();
      }
    }
  }

  private static void damage(ItemStack stack) {
    if (!stack.isDamageableItem() || !stack.is(FIRE_DAMAGEABLE)) return;
    if (MathUtil.nextInt(0, 2) != 0) return;
    int damage = MathUtil.clamp(Mth.floor(Math.sqrt(stack.getMaxDamage()) * 0.25), 1, 5);
    stack.setDamageValue(stack.getDamageValue() + damage);
  }
}
