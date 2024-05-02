package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.cooldown;

import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowballItem.class)
abstract class SnowballItemMixin extends Item {

    public SnowballItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V"), method = "use")
    private void andromeda$useCooldown(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient()) return;

        var config = world.am$get(Snowballs.CONFIG);
        if (!config.available || !config.extinguish) return;

        user.getItemCooldownManager().set(this, config.cooldown.asInt(() -> {
            LootContextParameterSet set = new LootContextParameterSet.Builder((ServerWorld) world)
                    .add(LootContextParameters.ORIGIN, user.getPos())
                    .add(LootContextParameters.THIS_ENTITY, user)
                    .add(LootContextParameters.TOOL, user.getStackInHand(hand))
                    .build(LootContextTypes.FISHING);

            return new LootContext.Builder(set).build(null);
        }));
    }
}
