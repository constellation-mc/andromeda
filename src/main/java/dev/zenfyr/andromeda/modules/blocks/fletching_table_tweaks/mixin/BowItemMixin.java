package dev.zenfyr.andromeda.modules.blocks.fletching_table_tweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.blocks.fletching_table_tweaks.FletchingScreenHandler;
import dev.zenfyr.andromeda.modules.blocks.fletching_table_tweaks.FletchingTableTweaks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
abstract class BowItemMixin extends ProjectileWeaponItem {

  public BowItemMixin(Properties settings) {
    super(settings);
  }

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/BowItem;shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V"),
      method = "releaseUsing",
      index = 6)
  public float andromeda$setVelocity(
      float f,
      @Local(argsOnly = true, name = "itemStack") ItemStack itemStack,
      @Local Player player) {
    int a = itemStack.getOrDefault(FletchingScreenHandler.TIGHTENED.get(), 0);
    if (a > 0) {
      itemStack.set(FletchingScreenHandler.TIGHTENED.get(), a - 1);
      return f * player.level().am$get(FletchingTableTweaks.CONFIG).divergenceModifier;
    }
    return f;
  }
}
