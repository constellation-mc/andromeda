package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.items.pouches.entities.PouchEntity;
import me.melontini.andromeda.modules.items.pouches.items.PouchItem;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.minecraft.util.ItemStackUtil;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

import static me.melontini.andromeda.common.Andromeda.id;

public class Main {

    public static final Keeper<PouchItem> SEED_POUCH = Keeper.create();
    public static final Keeper<PouchItem> FLOWER_POUCH = Keeper.create();
    public static final Keeper<PouchItem> SAPLING_POUCH = Keeper.create();
    public static final Keeper<PouchItem> SPECIAL_POUCH = Keeper.create();
    public static final Keeper<EntityType<PouchEntity>> POUCH = Keeper.create();

    private static final Map<BlockEntityType<?>, Field> VIEWABLE_BLOCKS = new HashMap<>();
    public static final Map<BlockEntityType<?>, Field> VIEWABLE_VIEW = Collections.unmodifiableMap(VIEWABLE_BLOCKS);

    public static int getViewCount(BlockEntity be) {
        Field f = Main.VIEWABLE_VIEW.get(be.getType());
        if (f != null) {
            ViewerCountManager vcm = (ViewerCountManager) Exceptions.supply(() -> f.get(be));
            return vcm.getViewerCount();
        }
        return -1;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void tryInsertItem(World world, Vec3d pos, ItemStack stack, Storage<ItemVariant> storage) {
        if (stack.isEmpty()) return;
        ItemStack itemStack = stack.copy();
        try (Transaction transaction = Transaction.openOuter()) {
            long i = StorageUtil.tryInsertStacking(storage, ItemVariant.of(stack), stack.getCount(), transaction);
            if (i > 0) {
                transaction.commit();
                itemStack.setCount((int) (stack.getCount() - i));
            }
        }
        if (!itemStack.isEmpty()) ItemStackUtil.spawnVelocity(pos, itemStack, world, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2);
    }

    Main(Pouches module, Pouches.Config config) {
        SEED_POUCH.init(RegistryUtil.register(config.seedPouch, Registries.ITEM, id("seed_pouch"),
                        () -> new PouchItem(PouchEntity.Type.SEED, new FabricItemSettings().maxCount(16))));

        FLOWER_POUCH.init(RegistryUtil.register(config.flowerPouch, Registries.ITEM, id("flower_pouch"),
                        () -> new PouchItem(PouchEntity.Type.FLOWER, new FabricItemSettings().maxCount(16))));

        SAPLING_POUCH.init(RegistryUtil.register(config.saplingPouch, Registries.ITEM, id("sapling_pouch"),
                        () -> new PouchItem(PouchEntity.Type.SAPLING, new FabricItemSettings().maxCount(16))));

        SPECIAL_POUCH.init(RegistryUtil.register(config.specialPouch, Registries.ITEM, id("special_pouch"),
                        () -> new PouchItem(PouchEntity.Type.CUSTOM, new FabricItemSettings().maxCount(16))));

        POUCH.init(RegistryUtil.register(Registries.ENTITY_TYPE, id("pouch"), () -> FabricEntityTypeBuilder.<PouchEntity>create(SpawnGroup.MISC, PouchEntity::new)
                .dimensions(new EntityDimensions(0.25F, 0.25F, true))
                .trackRangeChunks(4).trackedUpdateRate(10).build()));

        Trades.register();

        List<Keeper<PouchItem>> l = List.of(SEED_POUCH, FLOWER_POUCH, SAPLING_POUCH, SPECIAL_POUCH);
        AndromedaItemGroup.accept(acceptor -> acceptor.keepers(module, ItemGroups.TOOLS, new ArrayList<>(l)));

        var behavior = new ProjectileDispenserBehavior() {
            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                var pouch = new PouchEntity(position.getX(), position.getY(), position.getZ(), world);
                pouch.setPouchType(((PouchItem) stack.getItem()).getType());
                return pouch;
            }
        };

        for (Keeper<PouchItem> pouchItemKeeper : l) {
            pouchItemKeeper.ifPresent(pi -> DispenserBlock.registerBehavior(pi, behavior));
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
            if (f.getType() == ViewerCountManager.class) {
                return f;
            }
        }
        if (cls.getSuperclass() != null) return traverse(cls.getSuperclass());
        return null;
    }

    public static void testBlocks(Pouches module) {
        for (BlockEntityType<?> type : Registries.BLOCK_ENTITY_TYPE) {
            var o = type.blocks.stream().findAny();
            if (o.isPresent()) {
                try {
                    test(type.instantiate(BlockPos.ORIGIN, o.orElseThrow().getDefaultState()), module);
                } catch (Exception e) {
                    module.logger().error("{} failed the ViewerCountManager test. {}: {}", Registries.BLOCK_ENTITY_TYPE.getId(type), e.getClass().getSimpleName(), e.getLocalizedMessage());
                }
            } else {
                module.logger().warn("{} has no blocks?", Registries.BLOCK_ENTITY_TYPE.getId(type));
            }
        }
    }
}
