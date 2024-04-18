package me.melontini.andromeda.modules.blocks.better_fletching_table.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.dark_matter.api.data.nbt.NbtUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
abstract class BowItemMixin extends RangedWeaponItem {

    public BowItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V", shift = At.Shift.AFTER), method = "onStoppedUsing")
    public void andromeda$setVelocity(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, @Local PersistentProjectileEntity projectile, @Local PlayerEntity player, @Local(index = 9) float f) {
        NbtCompound stackNbt = stack.getNbt();
        int a = NbtUtil.getInt(stackNbt, "AM-Tightened", 0);
        if (a > 0) {
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, f * 3.0F, 0.2F);
            stackNbt.putInt("AM-Tightened", a - 1);
        }
    }
}
