package dev.zenfyr.andromeda.modules.mechanics.trading_goat_horn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.modules.mechanics.trading_goat_horn.CustomTraderManager;
import dev.zenfyr.andromeda.modules.mechanics.trading_goat_horn.GoatHorn;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
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
                  "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/ItemStack;I)V",
              shift = At.Shift.BEFORE),
      method = "use")
  private void andromeda$wanderingGoatHorn(
      Level level,
      Player player,
      InteractionHand hand,
      CallbackInfoReturnable<InteractionResult> cir,
      @Local(name = "instrumentHolder") Optional<? extends Holder<Instrument>> instrumentHolder) {
    if (level.isClientSide()) return;
    if (instrumentHolder
        .filter(holder -> holder.is(CustomTraderManager.TRADER_SONGS))
        .isEmpty()) return;

    ServerLevel sw = (ServerLevel) level;
    if (!sw.getGameRules().get(GameRules.SPAWN_MOBS)) return;
    var cfg = level.am$get(GoatHorn.CONFIG);
    if (!cfg.available) return;

    sw.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get())
        .trySpawn(
            (ServerLevel) level,
            sw.getServer().getWorldData().overworldData(),
            player.getItemInHand(hand),
            player,
            cfg.highlightTrader);
  }
}
