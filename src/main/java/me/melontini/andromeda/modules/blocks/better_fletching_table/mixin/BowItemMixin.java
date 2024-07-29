package me.melontini.andromeda.modules.blocks.better_fletching_table.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.blocks.better_fletching_table.BetterFletchingTable;
import me.melontini.dark_matter.api.data.nbt.NbtUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
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
                  "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V"),
      method = "onStoppedUsing",
      index = 5)
  public float andromeda$setVelocity(
      float f, @Local(ordinal = 0, argsOnly = true) ItemStack stack, @Local PlayerEntity player) {
    NbtCompound stackNbt = stack.getNbt();
    int a = NbtUtil.getInt(stackNbt, "AM-Tightened", 0);
    if (a > 0) {
      stackNbt.putInt("AM-Tightened", a - 1);
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
