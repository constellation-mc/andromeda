package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.common.util.ItemStackUtil;
import me.melontini.andromeda.modules.items.pouches.entities.PouchEntity;
import me.melontini.andromeda.modules.items.pouches.items.PouchItem;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {

    public static final Keeper<PouchItem> SEED_POUCH = Keeper.create();
    public static final Keeper<PouchItem> FLOWER_POUCH = Keeper.create();
    public static final Keeper<PouchItem> SAPLING_POUCH = Keeper.create();
    public static final Keeper<PouchItem> SPECIAL_POUCH = Keeper.create();
    public static final Keeper<EntityType<PouchEntity>> POUCH = Keeper.create();

    public static final Map<BlockEntityType<?>, Field> VIEWABLE_BLOCKS = new HashMap<>();

    public static int getViewCount(BlockEntity be) {
        Field f = Main.VIEWABLE_BLOCKS.get(be.getType());
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
        SEED_POUCH.init(ContentBuilder.ItemBuilder.create(id("seed_pouch"),
                        () -> new PouchItem(PouchEntity.Type.SEED, new FabricItemSettings().maxCount(16)))
                .itemGroup(CommonItemGroups.tools()).register(() -> config.seedPouch).build());

        FLOWER_POUCH.init(ContentBuilder.ItemBuilder.create(id("flower_pouch"),
                        () -> new PouchItem(PouchEntity.Type.FLOWER, new FabricItemSettings().maxCount(16)))
                .itemGroup(CommonItemGroups.tools()).register(() -> config.flowerPouch).build());

        SAPLING_POUCH.init(ContentBuilder.ItemBuilder.create(id("sapling_pouch"),
                        () -> new PouchItem(PouchEntity.Type.SAPLING, new FabricItemSettings().maxCount(16)))
                .itemGroup(CommonItemGroups.tools()).register(() -> config.saplingPouch).build());

        SPECIAL_POUCH.init(ContentBuilder.ItemBuilder.create(id("special_pouch"),
                        () -> new PouchItem(PouchEntity.Type.CUSTOM, new FabricItemSettings().maxCount(16)))
                .itemGroup(CommonItemGroups.tools()).register(() -> config.specialPouch).build());

        POUCH.init(RegistryUtil.createEntityType(id("pouch"), FabricEntityTypeBuilder.<PouchEntity>create(SpawnGroup.MISC, PouchEntity::new)
                .dimensions(new EntityDimensions(0.25F, 0.25F, true))
                .trackRangeChunks(4).trackedUpdateRate(10)));

        Trades.register();

        List<Keeper<PouchItem>> l = List.of(SEED_POUCH, FLOWER_POUCH, SAPLING_POUCH, SPECIAL_POUCH);
        AndromedaItemGroup.accept(acceptor -> acceptor.keepers(module, new ArrayList<>(l)));

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

    private static void test(BlockEntity be) {
        if (be != null) {
            Field f = traverse(be.getClass());
            if (f != null) {
                try {
                    f.setAccessible(true);
                    VIEWABLE_BLOCKS.put(be.getType(), f);
                } catch (Exception e) {
                    AndromedaLog.error("{}: {}", e.getClass(), e.getLocalizedMessage());
                }
            }
        }
    }

    private static Field traverse(Class<?> cls) {
        for (Field f : cls.getDeclaredFields()) {
            if (f.getType() == ViewerCountManager.class) {
                return f;
            }
        }
        if (cls.getSuperclass() != null) return traverse(cls.getSuperclass());
        return null;
    }

    public static void testBlocks() {
        for (BlockEntityType<?> type : CommonRegistries.blockEntityTypes()) {
            var o = type.blocks.stream().findAny();
            if (o.isPresent()) {
                try {
                    test(type.instantiate(BlockPos.ORIGIN, o.orElseThrow().getDefaultState()));
                } catch (Exception e) {
                    AndromedaLog.error("{} failed the ViewerCountManager test. {}: {}", CommonRegistries.blockEntityTypes().getId(type), e.getClass().getSimpleName(), e.getLocalizedMessage());
                }
            } else {
                AndromedaLog.warn("{} has no blocks?", CommonRegistries.blockEntityTypes().getId(type));
            }
        }
    }
}
