package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.mojang.serialization.MapCodec;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.command.CommandType;
import me.melontini.commander.api.command.Selector;
import me.melontini.commander.api.event.EventContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public record ItemPlopEffect(Selector.Conditioned selector) implements Command {

  public static final MapCodec<ItemPlopEffect> CODEC =
      Selector.CODEC.fieldOf("selector").xmap(ItemPlopEffect::new, ItemPlopEffect::selector);

  @Override
  public boolean execute(EventContext context) {
    var opt = selector.select(context);
    if (opt.isEmpty()) return false;
    Entity entity = opt.get().getEntity();
    if (entity instanceof ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeItemStack(context.lootContext().get(LootContextParameters.TOOL));
      ServerPlayNetworking.send(player, Main.COLORED_FLYING_STACK_LANDED, buf);
    } else if (entity instanceof LivingEntity living) {
      living.addStatusEffect(
          new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false, true));
    }
    return true;
  }

  @Override
  public CommandType type() {
    return Main.ITEM_PLOP_COMMAND.orThrow();
  }
}
