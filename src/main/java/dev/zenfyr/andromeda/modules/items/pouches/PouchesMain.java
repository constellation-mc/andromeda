package dev.zenfyr.andromeda.modules.items.pouches;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.items.pouches.entities.CustomPouchComponent;
import dev.zenfyr.andromeda.modules.items.pouches.entities.PouchEntity;
import dev.zenfyr.andromeda.modules.items.pouches.items.PouchItem;
import dev.zenfyr.andromeda.modules.misc.creative_mode_tab.AndromedaCreativeTab;
import dev.zenfyr.pulsar.api.itemstack.ItemStackUtil;
import dev.zenfyr.pulsar.api.util.ExceptionUtil;
import java.lang.reflect.Field;
import java.util.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PouchesMain {

  public static final Keeper<PouchItem> SEED_POUCH = Keeper.create();
  public static final Keeper<PouchItem> FLOWER_POUCH = Keeper.create();
  public static final Keeper<PouchItem> SAPLING_POUCH = Keeper.create();
  public static final Keeper<PouchItem> SPECIAL_POUCH = Keeper.create();
  public static final Keeper<EntityType<PouchEntity>> POUCH = Keeper.create();

  public static final Keeper<DataComponentType<CustomPouchComponent>> CUSTOM_COMPONENT =
      Keeper.create();

  private static final Map<BlockEntityType<?>, Field> VIEWABLE_BLOCKS = new HashMap<>();
  public static final Map<BlockEntityType<?>, Field> VIEWABLE_VIEW =
      Collections.unmodifiableMap(VIEWABLE_BLOCKS);

  public static int getViewCount(BlockEntity be) {
    Field f = PouchesMain.VIEWABLE_VIEW.get(be.getType());
    if (f != null) {
      ContainerOpenersCounter vcm = (ContainerOpenersCounter) ExceptionUtil.supply(() -> f.get(be));
      return vcm.getOpenerCount();
    }
    return -1;
  }

  @SuppressWarnings("UnstableApiUsage")
  public static void tryInsertItem(
      Level world, Vec3 pos, ItemStack stack, Storage<ItemVariant> storage) {
    if (stack.isEmpty()) return;
    ItemStack itemStack = stack.copy();
    try (Transaction transaction = Transaction.openOuter()) {
      long i = StorageUtil.tryInsertStacking(
          storage, ItemVariant.of(stack), stack.getCount(), transaction);
      if (i > 0) {
        transaction.commit();
        itemStack.setCount((int) (stack.getCount() - i));
      }
    }
    if (!itemStack.isEmpty())
      ItemStackUtil.spawnVelocity(pos, itemStack, world, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2);
  }

  static void init() {
    var module = ModuleManager.get().get(Pouches.class).orElseThrow();
    var config = Andromeda.MAIN.get(Pouches.MAIN_CONFIG);

    if (config.seedPouch) {
      var key = Andromeda.key(Registries.ITEM, "seed_pouch");
      SEED_POUCH.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new PouchItem(PouchEntity.Type.SEED, new Item.Properties().setId(key).stacksTo(16))));
    }

    if (config.flowerPouch) {
      var key = Andromeda.key(Registries.ITEM, "flower_pouch");
      FLOWER_POUCH.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new PouchItem(
              PouchEntity.Type.FLOWER, new Item.Properties().setId(key).stacksTo(16))));
    }

    if (config.saplingPouch) {
      var key = Andromeda.key(Registries.ITEM, "sapling_pouch");
      SAPLING_POUCH.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new PouchItem(
              PouchEntity.Type.SAPLING, new Item.Properties().setId(key).stacksTo(16))));
    }

    if (config.specialPouch) {
      var key = Andromeda.key(Registries.ITEM, "special_pouch");
      SPECIAL_POUCH.init(Registry.register(
          BuiltInRegistries.ITEM,
          key,
          new PouchItem(
              PouchEntity.Type.CUSTOM, new Item.Properties().setId(key).stacksTo(16))));
    }

    var key = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), id("pouch"));
    POUCH.init(Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        id("pouch"),
        EntityType.Builder.<PouchEntity>of(PouchEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(key)));

    CUSTOM_COMPONENT.init(Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("custom_loot"),
        DataComponentType.<CustomPouchComponent>builder()
            .persistent(CustomPouchComponent.CODEC)
            .networkSynchronized(CustomPouchComponent.PACKET_CODEC)
            .build()));

    List<Keeper<PouchItem>> pouches =
        List.of(SEED_POUCH, FLOWER_POUCH, SAPLING_POUCH, SPECIAL_POUCH);

    CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .register(entries -> {
          for (Keeper<PouchItem> pouch : pouches) {
            if (!pouch.isPresent()) continue;
            entries.accept(pouch.orThrow());
          }
        });

    if (ModuleManager.get().get("misc/creative_mode_tab").isPresent()) {
      AndromedaCreativeTab.BUS.listen(
          acceptor -> acceptor.keepers(module, new ArrayList<>(pouches)));
    }

    for (Keeper<PouchItem> pouchItemKeeper : pouches) {
      if (pouchItemKeeper.isPresent())
        DispenserBlock.registerBehavior(
            pouchItemKeeper.orThrow(), new ProjectileDispenseBehavior(pouchItemKeeper.orThrow()));
    }

    ServerLifecycleEvents.SERVER_STARTING.register(server -> testBlocks());
  }

  private static void test(BlockEntity be, Pouches module) {
    if (be != null) {
      Field f = traverse(be.getClass());
      if (f != null) {
        try {
          f.setAccessible(true);
          VIEWABLE_BLOCKS.put(be.getType(), f);
        } catch (Exception e) {
          module.logger().error("{}: {}", e.getClass(), e.getLocalizedMessage());
        }
      }
    }
  }

  private static @Nullable Field traverse(Class<?> cls) {
    for (Field f : cls.getDeclaredFields()) {
      if (f.getType() == ContainerOpenersCounter.class) {
        return f;
      }
    }
    if (cls.getSuperclass() != null) return traverse(cls.getSuperclass());
    return null;
  }

  static void testBlocks() {
    var manager = ModuleManager.get();
    var module = manager.get(Pouches.class).orElseThrow();
    for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
      var o = type.validBlocks.stream().findAny();
      if (o.isPresent()) {
        try {
          test(type.create(BlockPos.ZERO, o.orElseThrow().defaultBlockState()), module);
        } catch (Exception e) {
          module
              .logger()
              .error(
                  "{} failed the ViewerCountManager test. {}: {}",
                  BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type),
                  e.getClass().getSimpleName(),
                  e.getLocalizedMessage());
        }
      } else {
        module.logger().warn("{} has no blocks?", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type));
      }
    }

    if (manager.debug().isVerbose()) {
      StringBuilder b = new StringBuilder();
      b.append("Viewable block entities:");
      PouchesMain.VIEWABLE_VIEW.forEach((blockEntityType, field) -> {
        b.append('\n')
            .append(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType))
            .append(": ")
            .append(field.getName());
      });
      module.logger().info(b);
    }
  }
}
