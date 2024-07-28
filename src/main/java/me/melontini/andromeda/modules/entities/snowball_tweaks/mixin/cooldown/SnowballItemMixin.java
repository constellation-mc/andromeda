package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.cooldown;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SnowballItem;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SnowballItem.class)
abstract class SnowballItemMixin extends Item {

    public SnowballItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), method = "use")
    private Entity andromeda$useCooldown(Entity par1, @Local(argsOnly = true) World world, @Local(argsOnly = true) PlayerEntity user, @Local(argsOnly = true) Hand hand) {
        if (world.isClient()) return null;

        var config = world.am$get(Snowballs.CONFIG);
        if (!config.available.asBoolean(ConstantLootContextAccessor.get(par1))) return par1;

        var supplier = Memoize.supplier(LootContextUtil.fishing(world, user.getPos(), user.getStackInHand(hand), user));
        if (!config.enableCooldown.asBoolean(supplier)) return par1;

        user.getItemCooldownManager().set(this, config.cooldown.asInt(supplier));
        return par1;
    }
}
