package dev.zenfyr.andromeda.modules.mechanics.throwable_items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import dev.zenfyr.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.command.CommandType;
import me.melontini.commander.api.command.Selector;
import me.melontini.commander.api.event.EventContext;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record ParticleCommand(Selector.Conditioned selector, boolean item, Optional<Integer> colors)
    implements Command {

  public static final MapCodec<ParticleCommand> CODEC =
      RecordCodecBuilder.mapCodec(data -> data.group(
              Selector.CODEC.fieldOf("selector").forGetter(ParticleCommand::selector),
              ExtraCodecs.optional("item", Codec.BOOL, true).forGetter(ParticleCommand::item),
              ExtraCodecs.optional("colors", ExtraCodecs.COLOR).forGetter(ParticleCommand::colors))
          .apply(data, ParticleCommand::new));

  @Override
  public boolean execute(EventContext context) {
    var opt = selector.select(context);
    if (opt.isEmpty()) return false;

    sendParticlePacket(
        opt.get().getLevel(),
        opt.get().getPosition(),
        context.lootContext().getParamOrNull(LootContextParams.TOOL));
    return true;
  }

  @Override
  public CommandType type() {
    return Main.PARTICLE_COMMAND.orThrow();
  }

  public void sendParticlePacket(ServerLevel world, Vec3 pos, ItemStack stack) {
    FriendlyByteBuf byteBuf = PacketByteBufs.create();
    byteBuf.writeDouble(pos.x()).writeDouble(pos.y()).writeDouble(pos.z());
    byteBuf.writeBoolean(item);
    byteBuf.writeItem(stack);
    byteBuf.writeBoolean(colors.isPresent());
    byteBuf.writeVarInt(colors.orElse(-1));
    for (ServerPlayer serverPlayerEntity : PlayerLookup.tracking(world, BlockPos.containing(pos))) {
      ServerPlayNetworking.send(serverPlayerEntity, Main.FLYING_STACK_LANDED, byteBuf);
    }
  }
}
