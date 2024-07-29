package me.melontini.andromeda.modules.blocks.better_fletching_table.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.blocks.better_fletching_table.BetterFletchingTable;
import me.melontini.andromeda.modules.blocks.better_fletching_table.FletchingScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
abstract class BowItemMixin extends RangedWeaponItem {

  public BowItemMixin(Settings settings) {
    super(settings);
  }

  @ModifyArg(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/item/BowItem;shootAll(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/entity/LivingEntity;)V"), method = "onStoppedUsing", index = 5)
    public float andromeda$setVelocity(float f, @Local(ordinal = 0, argsOnly = true) ItemStack stack, @Local PlayerEntity player) {
    int a = stack.getOrDefault(FletchingScreenHandler.TIGHTENED.get(), 0);
    if (a > 0) {
      stack.set(FletchingScreenHandler.TIGHTENED.get(), a - 1);
      return f
          * player
              .world
              .am$get(BetterFletchingTable.CONFIG)
              .divergenceModifier
              .asFloat(LootContextUtil.fishing(player.world, player.getPos(), stack, player));
    }
    return f;
  }
}
