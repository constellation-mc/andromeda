package me.melontini.andromeda.modules.items.pouches;

import static me.melontini.andromeda.common.Andromeda.id;

import java.lang.reflect.Field;
import java.util.*;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.items.pouches.entities.PouchEntity;
import me.melontini.andromeda.modules.items.pouches.items.PouchItem;
import me.melontini.andromeda.util.Util;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.minecraft.util.ItemStackUtil;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Main {

  public static final Keeper<PouchItem> SEED_POUCH = Keeper.create();
  public static final Keeper<PouchItem> FLOWER_POUCH = Keeper.create();
  public static final Keeper<PouchItem> SAPLING_POUCH = Keeper.create();
  public static final Keeper<PouchItem> SPECIAL_POUCH = Keeper.create();
  public static final Keeper<EntityType<PouchEntity>> POUCH = Keeper.create();

  private static final Map<BlockEntityType<?>, Field> VIEWABLE_BLOCKS = new HashMap<>();
  public static final Map<BlockEntityType<?>, Field> VIEWABLE_VIEW =
      Collections.unmodifiableMap(VIEWABLE_BLOCKS);

  public static int getViewCount(BlockEntity be) {
    Field f = Main.VIEWABLE_VIEW.get(be.getType());
    if (f != null) {
      ContainerOpenersCounter vcm = (ContainerOpenersCounter) Exceptions.supply(() -> f.get(be));
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
    SEED_POUCH.init(RegistryUtil.register(
        config.seedPouch,
        BuiltInRegistries.ITEM,
        id("seed_pouch"),
        () -> new PouchItem(PouchEntity.Type.SEED, new FabricItemSettings().stacksTo(16))));

    FLOWER_POUCH.init(RegistryUtil.register(
        config.flowerPouch,
        BuiltInRegistries.ITEM,
        id("flower_pouch"),
        () -> new PouchItem(PouchEntity.Type.FLOWER, new FabricItemSettings().stacksTo(16))));

    SAPLING_POUCH.init(RegistryUtil.register(
        config.saplingPouch,
        BuiltInRegistries.ITEM,
        id("sapling_pouch"),
        () -> new PouchItem(PouchEntity.Type.SAPLING, new FabricItemSettings().stacksTo(16))));

    SPECIAL_POUCH.init(RegistryUtil.register(
        config.specialPouch,
        BuiltInRegistries.ITEM,
        id("special_pouch"),
        () -> new PouchItem(PouchEntity.Type.CUSTOM, new FabricItemSettings().stacksTo(16))));

    POUCH.init(RegistryUtil.register(
        BuiltInRegistries.ENTITY_TYPE,
        id("pouch"),
        () -> FabricEntityTypeBuilder.<PouchEntity>create(MobCategory.MISC, PouchEntity::new)
            .dimensions(new EntityDimensions(0.25F, 0.25F, true))
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .build()));

    Trades.register();

    List<Keeper<PouchItem>> l = List.of(SEED_POUCH, FLOWER_POUCH, SAPLING_POUCH, SPECIAL_POUCH);
    AndromedaItemGroup.BUS.listen(acceptor ->
        acceptor.keepers(module, CreativeModeTabs.TOOLS_AND_UTILITIES, new ArrayList<>(l)));

    var behavior = new AbstractProjectileDispenseBehavior() {
      @Override
      protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
        var pouch = new PouchEntity(position.x(), position.y(), position.z(), world);
        pouch.setPouchType(((PouchItem) stack.getItem()).getType());
        return pouch;
      }
    };

    for (Keeper<PouchItem> pouchItemKeeper : l) {
      if (pouchItemKeeper.isPresent())
        DispenserBlock.registerBehavior(pouchItemKeeper.orThrow(), behavior);
    }
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
    var module = ModuleManager.get().get(Pouches.class).orElseThrow();
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

    if (Util.isDev()) {
      StringBuilder b = new StringBuilder();
      b.append("Viewable block entities:");
      Main.VIEWABLE_VIEW.forEach((blockEntityType, field) -> {
        b.append('\n')
            .append(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType))
            .append(": ")
            .append(field.getName());
      });
      module.logger().info(b);
    }
  }
}
