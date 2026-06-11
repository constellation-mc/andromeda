package dev.zenfyr.andromeda.modules.mechanics.trading_goat_horn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.mechanics.trading_goat_horn.CustomTraderManager;
import dev.zenfyr.andromeda.modules.mechanics.trading_goat_horn.GoatHorn;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InstrumentItem.class)
abstract class GoatHornMixin {

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V",
              shift = At.Shift.BEFORE),
      method = "use")
  private void andromeda$wanderingGoatHorn(
      Level world,
      Player user,
      InteractionHand hand,
      CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
      @Local Optional<? extends Holder<Instrument>> optional) {
    if (world.isClientSide()) return;
    if (optional.filter(holder -> holder.is(CustomTraderManager.TRADER_SONGS)).isEmpty()) return;

    ServerLevel sw = (ServerLevel) world;
    if (!sw.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
    var cfg = world.am$get(GoatHorn.CONFIG);
    if (!cfg.available) return;

    sw.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get())
        .trySpawn(
            (ServerLevel) world,
            sw.getServer().getWorldData().overworldData(),
            user.getItemInHand(hand),
            user,
            cfg.highlightTrader);
  }
}
