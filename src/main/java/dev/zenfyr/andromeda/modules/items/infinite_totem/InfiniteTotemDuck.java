package dev.zenfyr.andromeda.modules.items.infinite_totem;

import dev.zenfyr.pulsar.api.util.tuple.Tuple;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public interface InfiniteTotemDuck {

  AtomicInteger andromeda$ascensionTicks();

  ItemEntity andromeda$ascensionItem();

  void andromeda$ascensionItem(ItemEntity item);

  Tuple<BeaconBlockEntity, Boolean> andromeda$beacon();

  void andromeda$beacon(Tuple<BeaconBlockEntity, Boolean> beacon);
}
