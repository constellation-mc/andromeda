package me.melontini.andromeda.modules.mechanics.trading_goat_horn.mixin;

import me.melontini.andromeda.modules.mechanics.trading_goat_horn.CustomTraderManager;
import me.melontini.andromeda.modules.mechanics.trading_goat_horn.GoatHorn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;
import java.util.Optional;

@Mixin(GoatHornItem.class)
abstract class GoatHornMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, method = "use")
    private void andromeda$wanderingGoatHorn(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, ItemStack itemStack, Optional<RegistryEntry<Instrument>> optional, Instrument instrument) {
        NbtCompound nbtCompound = itemStack.getNbt();
        if (world.isClient() || nbtCompound == null || nbtCompound.getString("instrument") == null) return;
        if (!Objects.equals(nbtCompound.getString("instrument"), "minecraft:sing_goat_horn")) return;

        ServerWorld sw = (ServerWorld) world;
        if (!sw.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) || !world.am$get(GoatHorn.class).enabled) return;

        sw.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get()).trySpawn((ServerWorld) world, sw.getServer().getSaveProperties().getMainWorldProperties(), user);
    }
}
