package dev.zenfyr.andromeda.modules.items.infinite_totem.client;

import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemDuck;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemMain;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.StartAscensionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class InfiniteTotemClient {

  public static void init() {
    ClientPlayNetworking.registerGlobalReceiver(
        StartAscensionPayload.ID,
        (payload, context) -> context.client().execute(() -> {
          ItemEntity item = (ItemEntity) context.client().level.getEntity(payload.item());
          ItemEntity pair = (ItemEntity) context.client().level.getEntity(payload.pair());
          if (item == null || pair == null) return;

          if (payload.start()) {
            ((InfiniteTotemDuck) item).andromeda$ascensionItem(pair);
            ((InfiniteTotemDuck) pair).andromeda$ascensionItem(item);
          } else {
            ((InfiniteTotemDuck) item).andromeda$ascensionItem(null);
            ((InfiniteTotemDuck) pair).andromeda$ascensionItem(null);
          }
        }));

    if (InfiniteTotemMain.KNOCKOFF_TOTEM_PARTICLE.isPresent()) {
      ParticleProviderRegistry.getInstance()
          .register(
              InfiniteTotemMain.KNOCKOFF_TOTEM_PARTICLE.orThrow(),
              KnockoffTotemParticle.Factory::new);
    }
  }

  public static ItemStack findTotem(LivingEntity entity) {
    for (InteractionHand hand : InteractionHand.values()) {
      ItemStack itemStack = entity.getItemInHand(hand);
      if (itemStack.has(DataComponents.DEATH_PROTECTION)) {
        return itemStack;
      }
    }
    return ItemStack.EMPTY;
  }

  public static void clientTotemItemTick(ItemEntity item) {
    var pair = ((InfiniteTotemDuck) item).andromeda$ascensionItem();
    if (pair == null) return;
    if (!InfiniteTotemMain.beaconCheck(item.level(), item)) {
      return;
    }

    item.setDeltaMovement(0, 0.07, 0);
    pair.setDeltaMovement(0, 0.07, 0);
  }
}
