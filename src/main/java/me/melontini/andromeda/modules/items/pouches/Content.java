package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.common.util.ItemStackUtil;
import me.melontini.andromeda.modules.items.pouches.entities.PouchEntity;
import me.melontini.andromeda.modules.items.pouches.items.PouchItem;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.melontini.andromeda.common.registries.Common.id;

public class Content {

    private static Pouches MODULE;

    public static final Keeper<PouchItem> SEED_POUCH = Common.start(() -> ContentBuilder.ItemBuilder.create(id("seed_pouch"),
                    () -> new PouchItem(PouchEntity.Type.SEED, new FabricItemSettings().maxCount(16)))
            .itemGroup(CommonItemGroups.tools()).register(() -> MODULE.config().seedPouch));

    public static final Keeper<PouchItem> FLOWER_POUCH = Common.start(() -> ContentBuilder.ItemBuilder.create(id("flower_pouch"),
                    () -> new PouchItem(PouchEntity.Type.FLOWER, new FabricItemSettings().maxCount(16)))
            .itemGroup(CommonItemGroups.tools()).register(() -> MODULE.config().flowerPouch));

    public static final Keeper<PouchItem> SAPLING_POUCH = Common.start(() -> ContentBuilder.ItemBuilder.create(id("sapling_pouch"),
                    () -> new PouchItem(PouchEntity.Type.SAPLING, new FabricItemSettings().maxCount(16)))
            .itemGroup(CommonItemGroups.tools()).register(() -> MODULE.config().saplingPouch));

    public static final Keeper<PouchItem> SPECIAL_POUCH = Common.start(() -> ContentBuilder.ItemBuilder.create(id("special_pouch"),
                    () -> new PouchItem(PouchEntity.Type.CUSTOM, new FabricItemSettings().maxCount(16)))
            .itemGroup(CommonItemGroups.tools()).register(() -> MODULE.config().specialPouch));

    public static final Keeper<EntityType<PouchEntity>> POUCH = Keeper.of(() ->
            RegistryUtil.createEntityType(id("pouch"),
                    FabricEntityTypeBuilder.<PouchEntity>create(SpawnGroup.MISC, PouchEntity::new)
                            .dimensions(new EntityDimensions(0.25F, 0.25F, true))
                            .trackRangeChunks(4).trackedUpdateRate(10)));

    public static final Map<BlockEntityType<?>, Field> VIEWABLE_BLOCKS = new HashMap<>();

    public static int getViewCount(BlockEntity be) {
        Field f = Content.VIEWABLE_BLOCKS.get(be.getType());
        if (f != null) {
            ViewerCountManager vcm = (ViewerCountManager) Exceptions.supply(() -> f.get(be));
            return vcm.getViewerCount();
        }
        return 0;
    }

    public static void tryInsertItem(World world, Vec3d pos, ItemStack stack, Inventory inventory) {
        int slot = getEmptyOrIdenticalSlotIndex(stack, inventory);
        if (slot == -1) {
            ItemStackUtil.spawnVelocity(pos, stack, world, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2);
        } else {
            if (!inventory.getStack(slot).isEmpty()) {
                int count = inventory.getStack(slot).getCount();
                int stackCount = stack.getCount();
                stack.setCount(stackCount + count);
                inventory.setStack(slot, stack);
            } else {
                inventory.setStack(slot, stack);
            }
        }
    }

    public static int getEmptyOrIdenticalSlotIndex(ItemStack stack, Inventory inventory) {
        assert inventory != null;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() == stack.getItem()) {
                int a = stack.getCount();
                int b = inventory.getStack(i).getCount();
                if (!((a + b) > stack.getMaxCount())) {
                    return i;
                }
            } else if (inventory.getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static void init() {
        for (Block block : CommonRegistries.blocks()) {
            test(block);
        }
        RegistryEntryAddedCallback.event(CommonRegistries.blocks()).register((rawId, id, object) -> test(object));

        Trades.register();

        var l = List.of(SEED_POUCH, FLOWER_POUCH, SAPLING_POUCH, SPECIAL_POUCH);
        AndromedaItemGroup.accept(acceptor -> acceptor.keepers(MODULE, l));

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

    private static void test(Block block) {
        if (block instanceof BlockEntityProvider be) {
            var real = be.createBlockEntity(BlockPos.ORIGIN, block.getDefaultState());
            if (real != null) {
                Field f = traverse(real.getClass());
                if (f != null) {
                    try {
                        f.setAccessible(true);
                        VIEWABLE_BLOCKS.put(real.getType(), f);
                    } catch (Exception e) {
                        AndromedaLog.error("{}: {}", e.getClass(), e.getLocalizedMessage());
                    }
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
}
