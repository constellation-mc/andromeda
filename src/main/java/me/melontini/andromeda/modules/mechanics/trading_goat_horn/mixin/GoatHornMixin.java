package me.melontini.andromeda.modules.mechanics.trading_goat_horn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Objects;
import java.util.Optional;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.mechanics.trading_goat_horn.CustomTraderManager;
import me.melontini.andromeda.modules.mechanics.trading_goat_horn.GoatHorn;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;
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

    ResourceLocation identifier = optional.orElseThrow().unwrapKey().orElseThrow().location();
    if (!Objects.equals(identifier, world.am$get(GoatHorn.CONFIG).instrumentId)) return;

    ServerLevel sw = (ServerLevel) world;
    if (!sw.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
    var cfg = world.am$get(GoatHorn.CONFIG);
    var context = LootContextBuilder.fishing(
        user.level, builder -> builder.origin(user).tool(user, hand).thisEntity(user));
    if (!cfg.available.asBoolean(context)) return;

    sw.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get())
        .trySpawn(
            (ServerLevel) world,
            sw.getServer().getWorldData().overworldData(),
            user.getItemInHand(hand),
            user,
            cfg.highlightTrader.asBoolean(context));
  }
}
