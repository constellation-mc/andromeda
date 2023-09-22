package me.melontini.andromeda.mixin.blocks.better_fletching_table;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
@MixinRelatedConfigOption("usefulFletching")
public abstract class BowItemMixin extends RangedWeaponItem {
    public BowItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V", shift = At.Shift.AFTER), method = "onStoppedUsing", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void andromeda$setVelocity(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
        if (!Config.get().usefulFletching) return;

        NbtCompound stackNbt = stack.getNbt();
        int a = NbtUtil.getInt(stackNbt, "AM-Tightened", 0);
        if (a > 0) {
            persistentProjectileEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, f * 3.0F, 0.2F);
            stackNbt.putInt("AM-Tightened", a - 1);
            stack.setNbt(stackNbt);
        }
    }
}
