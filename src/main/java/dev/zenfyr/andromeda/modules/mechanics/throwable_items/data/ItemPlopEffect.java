package dev.zenfyr.andromeda.modules.mechanics.throwable_items.data;

import com.mojang.serialization.MapCodec;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.command.CommandType;
import me.melontini.commander.api.command.Selector;
import me.melontini.commander.api.event.EventContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record ItemPlopEffect(Selector.Conditioned selector) implements Command {

  public static final MapCodec<ItemPlopEffect> CODEC =
      Selector.CODEC.fieldOf("selector").xmap(ItemPlopEffect::new, ItemPlopEffect::selector);

  @Override
  public boolean execute(EventContext context) {
    var opt = selector.select(context);
    if (opt.isEmpty()) return false;
    Entity entity = opt.get().getEntity();
    if (entity instanceof ServerPlayer player) {
      FriendlyByteBuf buf = PacketByteBufs.create();
      buf.writeItem(context.lootContext().getParamOrNull(LootContextParams.TOOL));
      ServerPlayNetworking.send(player, Main.COLORED_FLYING_STACK_LANDED, buf);
    } else if (entity instanceof LivingEntity living) {
      living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, true));
    }
    return true;
  }

  @Override
  public CommandType type() {
    return Main.ITEM_PLOP_COMMAND.orThrow();
  }
}
