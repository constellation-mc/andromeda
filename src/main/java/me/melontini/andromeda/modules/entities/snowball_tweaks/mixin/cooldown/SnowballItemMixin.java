package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.cooldown;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
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
    if (!config.available.asBoolean(ConstantLootContextAccessor.get(par1))) return par1;

    var supplier = LootContextBuilder.fishing(
        world, builder -> builder.origin(user).tool(user, hand).thisEntity(user));
    if (!config.enableCooldown.asBoolean(supplier)) return par1;

    user.getCooldowns().addCooldown(this, config.cooldown.asInt(supplier));
    return par1;
  }
}
