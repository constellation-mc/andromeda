package dev.zenfyr.andromeda.modules.entities.snowball_tweaks.mixin.cooldown;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SnowballItem.class)
abstract class SnowballItemMixin extends Item {

  public SnowballItemMixin(Properties settings) {
    super(settings);
  }

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
      method = "use")
  private Entity andromeda$useCooldown(
      Entity par1,
      @Local(argsOnly = true) Level world,
      @Local(argsOnly = true) Player user,
      @Local(argsOnly = true) InteractionHand hand) {
    if (world.isClientSide()) return null;

    var config = world.am$get(Snowballs.CONFIG);
    if (!config.available) return par1;

    if (!config.enableCooldown) return par1;

    user.getCooldowns().addCooldown(this, config.cooldown);
    return par1;
  }
}
