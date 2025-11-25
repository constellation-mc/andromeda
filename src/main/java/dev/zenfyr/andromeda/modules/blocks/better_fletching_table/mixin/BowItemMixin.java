package dev.zenfyr.andromeda.modules.blocks.better_fletching_table.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.blocks.better_fletching_table.BetterFletchingTable;
import me.melontini.dark_matter.api.data.nbt.NbtUtil;
import net.minecraft.nbt.CompoundTag;
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
                  "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"),
      method = "releaseUsing",
      index = 5)
  public float andromeda$setVelocity(
      float f, @Local(ordinal = 0, argsOnly = true) ItemStack stack, @Local Player player) {
    CompoundTag stackNbt = stack.getTag();
    int a = NbtUtil.getInt(stackNbt, "AM-Tightened", 0);
    if (a > 0) {
      stackNbt.putInt("AM-Tightened", a - 1);
      return f * player.level.am$get(BetterFletchingTable.CONFIG).divergenceModifier;
    }
    return f;
  }
}
