package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.cooldown;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowballItem.class)
abstract class SnowballItemMixin extends Item {
    @Unique
    private static final Snowballs am$snow = ModuleManager.quick(Snowballs.class);

    public SnowballItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "use")
    private void andromeda$useCooldown(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!am$snow.config().enableCooldown) return;

        user.getItemCooldownManager().set(this, am$snow.config().cooldown);
    }
}
