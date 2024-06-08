package me.melontini.andromeda.modules.mechanics.trading_goat_horn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.mechanics.trading_goat_horn.CustomTraderManager;
import me.melontini.andromeda.modules.mechanics.trading_goat_horn.GoatHorn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;

@Mixin(GoatHornItem.class)
abstract class GoatHornMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V", shift = At.Shift.BEFORE), method = "use")
    private void andromeda$wanderingGoatHorn(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, @Local Optional<? extends RegistryEntry<Instrument>> optional) {
        if (world.isClient()) return;

        Identifier identifier = optional.orElseThrow().getKey().orElseThrow().getValue();
        if (!Objects.equals(identifier, world.am$get(GoatHorn.CONFIG).instrumentId)) return;

        ServerWorld sw = (ServerWorld) world;
        if (!sw.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) || !world.am$get(GoatHorn.CONFIG).available.asBoolean(
                LootContextUtil.fishing(user.world, user.getPos(), user.getStackInHand(hand), user))) return;

        sw.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get()).trySpawn((ServerWorld) world, sw.getServer().getSaveProperties().getMainWorldProperties(), user.getStackInHand(hand), user);
    }
}
