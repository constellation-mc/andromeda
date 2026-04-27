package dev.zenfyr.andromeda.modules.entities.snowball_tweaks.mixin.cooldown;

import dev.zenfyr.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowballItem.class)
abstract class SnowballItemMixin extends Item {

  public SnowballItemMixin(Properties settings) {
    super(settings);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"),
      method = "use")
  private void andromeda$useCooldown(
      Level level,
      Player player,
      InteractionHand hand,
      CallbackInfoReturnable<InteractionResult> cir) {
    var config = level.am$get(Snowballs.CONFIG);
    if (!config.available) return;

    if (!config.enableCooldown) return;

    player.getCooldowns().addCooldown(player.getItemInHand(hand), config.cooldown);
  }
}
