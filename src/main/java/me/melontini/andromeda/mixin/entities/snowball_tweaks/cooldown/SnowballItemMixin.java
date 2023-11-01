package me.melontini.andromeda.mixin.entities.snowball_tweaks.cooldown;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowballItem.class)
@Feature("snowballs.enableCooldown")
abstract class SnowballItemMixin extends Item {

    public SnowballItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "use")
    private void andromeda$useCooldown(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!Config.get().snowballs.enableCooldown) return;

        user.getItemCooldownManager().set(this, Config.get().snowballs.cooldown);
    }
}
