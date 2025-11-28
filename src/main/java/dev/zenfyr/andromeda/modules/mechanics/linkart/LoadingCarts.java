package dev.zenfyr.andromeda.modules.mechanics.linkart;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class LoadingCarts {

  public static final Codec<LoadingCarts> CODEC =
      RecordCodecBuilder.create(data -> data.group(BlockPos.CODEC
              .listOf()
              .xmap(Set::copyOf, List::copyOf)
              .fieldOf("reloads")
              .forGetter(o -> {
                Set<BlockPos> posSet = new HashSet<>();
                for (AbstractMinecart cart : o.carts) {
                  if (!cart.isRemoved()) posSet.add(cart.blockPosition());
                }
                return posSet;
              }))
          .apply(data, LoadingCarts::new));

  public static LoadingCarts get(Level level) {
    return ((ServerLevel) level).getAttachedOrCreate(Main.ATTACHMENT.get());
  }

  private final Set<AbstractMinecart> carts = new HashSet<>();
  private final Set<BlockPos> reloads = new HashSet<>();

  public LoadingCarts(Set<BlockPos> reloads) {
    this.reloads.addAll(reloads);
  }

  public void tick(ServerLevel level) {
    if (!this.reloads.isEmpty()) {
      for (BlockPos reload : this.reloads) {
        level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(reload), 4, reload);
      }
      this.reloads.clear();
    }
  }

  public void addCart(AbstractMinecart cart) {
    this.carts.add(cart);
  }

  public void removeCart(AbstractMinecart cart) {
    this.carts.remove(cart);
  }
}
