package dev.zenfyr.andromeda.modules.items.infinite_totem;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.StartAscensionPayload;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.AndromedaCreativeTab;
import dev.zenfyr.pulsar.api.util.tuple.Tuple;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;

public final class InfiniteTotemMain {

  public static final ResourceKey<Item> INFINITE_TOTEM_KEY =
      Andromeda.key(Registries.ITEM, "infinite_totem");
  public static final Keeper<Item> INFINITE_TOTEM = Keeper.create();
  public static final Keeper<SimpleParticleType> KNOCKOFF_TOTEM_PARTICLE = Keeper.create();

  public static final Identifier START_ASCENSION = Andromeda.id("start_ascension");

  public static final Tuple<BeaconBlockEntity, Boolean> NULL_BEACON = Tuple.of(null, false);

  static void init() {
    var module = ModuleManager.get().get(InfiniteTotem.class).orElseThrow();

    INFINITE_TOTEM.init(Registry.register(
        BuiltInRegistries.ITEM,
        INFINITE_TOTEM_KEY,
        new Item(new Item.Properties()
            .setId(INFINITE_TOTEM_KEY)
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .component(DataComponents.DEATH_PROTECTION, DeathProtection.TOTEM_OF_UNDYING))));

    KNOCKOFF_TOTEM_PARTICLE.init(Registry.register(
        BuiltInRegistries.PARTICLE_TYPE,
        id("knockoff_totem_particles"),
        FabricParticleTypes.simple()));

    PayloadTypeRegistry.clientboundPlay()
        .register(StartAscensionPayload.ID, StartAscensionPayload.CODEC);

    CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
        .register(entries -> entries.accept(INFINITE_TOTEM.orThrow()));

    if (ModuleManager.get().get("misc/creative_mode_tab").isPresent()) {
      AndromedaCreativeTab.BUS.listen(acceptor -> acceptor.keeper(module, INFINITE_TOTEM));
    }
  }

  public static void serverTotemItemTick(ServerLevel level, ItemEntity item) {
    var c = level.am$get(InfiniteTotem.CONFIG);
    if (!c.available || !c.enableAscension) return;
    InfiniteTotemDuck duck = ((InfiniteTotemDuck) item);

    AtomicInteger ascensionTicks = duck.andromeda$ascensionTicks();
    if (item.tickCount % 35 == 0 && ascensionTicks.get() == 0) {
      if (!beaconCheck(level, item)) {
        item.setDefaultPickUpDelay();
        if (duck.andromeda$ascensionItem() != null)
          duck.andromeda$ascensionItem().setDefaultPickUpDelay();
      }
    }

    var beacon = duck.andromeda$beacon();
    if (beacon.left() == null || !beacon.right()) return;

    if (duck.andromeda$ascensionItem() == null) {
      tickNoItem(level, item);
    } else {
      tickWithItem(level, item);
    }
  }

  private static void tickWithItem(ServerLevel level, ItemEntity item) {
    InfiniteTotemDuck duck = ((InfiniteTotemDuck) item);
    ItemEntity pair = duck.andromeda$ascensionItem();

    if (!beaconCheck(level, item)) {
      item.setDefaultPickUpDelay();
      pair.setDefaultPickUpDelay();

      duck.andromeda$ascensionItem(null);
      ((InfiniteTotemDuck) pair).andromeda$ascensionItem(null);

      var payload = new StartAscensionPayload(item.getId(), pair.getId(), false);
      for (ServerPlayer serverPlayerEntity : PlayerLookup.tracking(item)) {
        ServerPlayNetworking.send(serverPlayerEntity, payload);
      }

      return;
    }

    item.setDeltaMovement(0, 0.07, 0);
    pair.setDeltaMovement(0, 0.07, 0);

    duck.andromeda$ascensionTicks().incrementAndGet();

    if (duck.andromeda$ascensionTicks().get() != 180) return;
    duck.andromeda$ascensionTicks().set(0);

    level.sendParticles(
        ParticleTypes.END_ROD, item.getX(), item.getY(), item.getZ(), 15, 0, 0, 0, 0.4);

    ItemEntity entity = new ItemEntity(
        level,
        item.getX(),
        item.getY(),
        item.getZ(),
        new ItemStack(InfiniteTotemMain.INFINITE_TOTEM.orThrow()));
    item.discard();
    pair.discard();
    level.addFreshEntity(entity);
  }

  private static void tickNoItem(ServerLevel level, ItemEntity item) {
    InfiniteTotemDuck duck = ((InfiniteTotemDuck) item);
    AtomicInteger ascensionTicks = duck.andromeda$ascensionTicks();

    if (ascensionTicks.get() > 0) ascensionTicks.decrementAndGet();

    if (item.tickCount % 20 != 0) return;
    Optional<ItemEntity> optional = level
        .getEntitiesOfClass(
            ItemEntity.class,
            item.getBoundingBox().inflate(0.5),
            itemEntity -> itemEntity.getItem().is(Items.NETHER_STAR)
                && ((InfiniteTotemDuck) itemEntity).andromeda$ascensionItem() == null)
        .stream()
        .findAny();
    if (optional.isEmpty()) return;
    var ascItem = optional.get();

    duck.andromeda$ascensionItem(ascItem);
    ((InfiniteTotemDuck) ascItem).andromeda$ascensionItem(item);

    ItemStack targetStack = ascItem.getItem();
    int count = targetStack.getCount() - 1;
    if (count > 0) {
      ItemStack newStack = targetStack.copy();
      newStack.setCount(count);
      targetStack.setCount(1);

      ascItem.setItem(targetStack);
      ascItem.getEntityData().set(ItemEntity.DATA_ITEM, targetStack, true);

      ItemEntity entity =
          new ItemEntity(level, ascItem.getX(), ascItem.getY(), ascItem.getZ(), newStack);
      level.addFreshEntity(entity);
    }

    var payload = new StartAscensionPayload(item.getId(), ascItem.getId(), true);
    for (ServerPlayer serverPlayerEntity : PlayerLookup.tracking(item)) {
      ServerPlayNetworking.send(serverPlayerEntity, payload);
    }

    ascItem.setNeverPickUp();
    item.setNeverPickUp();
  }

  public static boolean beaconCheck(Level level, ItemEntity item) {
    BlockPos pos = item.blockPosition();
    InfiniteTotemDuck duck = ((InfiniteTotemDuck) item);

    BlockEntity entity = level.getBlockEntity(
        pos.atY(level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1));
    if (entity instanceof BeaconBlockEntity beaconBlock) {
      duck.andromeda$beacon(
          Tuple.of(beaconBlock, BeaconUtil.matchesPattern(level, beaconBlock.getBlockPos())));
      return true;
    } else {
      duck.andromeda$beacon(NULL_BEACON);
      return false;
    }
  }
}
