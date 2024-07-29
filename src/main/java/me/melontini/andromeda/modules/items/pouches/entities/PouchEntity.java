package me.melontini.andromeda.modules.items.pouches.entities;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.common.util.WorldUtil;
import me.melontini.andromeda.modules.items.pouches.Main;
import me.melontini.andromeda.modules.items.pouches.items.PouchItem;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.util.ItemStackUtil;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public class PouchEntity extends ThrownItemEntity {

    private static final TrackedData<Integer> POUCH_TYPE = DataTracker.registerData(PouchEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public PouchEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
        setItem(new ItemStack(getPouchType().getDefaultItem()));
    }

    public PouchEntity(double d, double e, double f, World world) {
        super(Main.POUCH.orThrow(), d, e, f, world);
        setItem(new ItemStack(getPouchType().getDefaultItem()));
    }

    public PouchEntity(LivingEntity livingEntity, World world) {
        super(Main.POUCH.orThrow(), livingEntity, world);
        setItem(new ItemStack(getPouchType().getDefaultItem()));
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();

        ItemStack stack = getStack();
        if (type == HitResult.Type.ENTITY) {
            this.onEntityHit((EntityHitResult) hitResult);
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), getX(), getY(), getZ(), 10, 0.2, 0.2, 0.2, 0.25);
            }
            this.discard();
        } else if (type == HitResult.Type.BLOCK) {
            this.onBlockHit((BlockHitResult) hitResult);
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), getX(), getY(), getZ(), 10, 0.2, 0.2, 0.2, 0.25);
            }
            this.discard();
        }

        if (type != HitResult.Type.MISS) {
            this.emitGameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!world.isClient()) {
            var stacks = WorldUtil.prepareLoot(world, this.getPouchType().getLootId(getStack()));

            Entity entity = entityHitResult.getEntity();
            if (entity instanceof PlayerEntity pe) {
                stacks.forEach(stack -> pe.getInventory().offerOrDrop(stack));
                return;
            } else if (entity instanceof InventoryOwner io) {
                var storage = InventoryStorage.of(io.getInventory(), null);
                stacks.forEach(stack -> Main.tryInsertItem(world, this.getPos(), stack, storage));
                return;
            } else if (entity instanceof Inventory inv) {
                var storage = InventoryStorage.of(inv, null);
                stacks.forEach(stack -> Main.tryInsertItem(world, this.getPos(), stack, storage));
                return;
            }
            stacks.forEach(stack -> ItemStackUtil.spawnVelocity(this.getPos(), stack, world, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2));
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!world.isClient()) {
            var stacks = WorldUtil.prepareLoot(world, this.getPouchType().getLootId(getStack()));

            var be = world.getBlockEntity(blockHitResult.getBlockPos());
            if ((be != null && Main.getViewCount(be) > 0)) {
                var storage = ItemStorage.SIDED.find(world, blockHitResult.getBlockPos(), world.getBlockState(blockHitResult.getBlockPos()), be, blockHitResult.getSide());
                if (storage != null) {
                    stacks.forEach(stack -> Main.tryInsertItem(world, this.getPos(), stack, storage));
                    return;
                }
            }
            stacks.forEach(stack -> ItemStackUtil.spawnVelocity(this.getPos(), stack, world, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2));
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(POUCH_TYPE, Type.SEED.syncId);
    }

    @Override
    protected PouchItem getDefaultItem() {
        return Type.SEED.getDefaultItem();
    }

    public Type getPouchType() {
        return Type.getType(this.dataTracker.get(POUCH_TYPE));
    }

    public void setPouchType(Type type) {
        this.dataTracker.set(POUCH_TYPE, type.syncId);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Type", getPouchType().name());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Type")) {
            setPouchType(Type.valueOf(nbt.getString("Type")));
        }
    }

    public enum Type {
        SEED(0, RegistryKey.of(RegistryKeys.LOOT_TABLE, Andromeda.id("pouches/seeds")), Main.SEED_POUCH),
        SAPLING(1, RegistryKey.of(RegistryKeys.LOOT_TABLE, Andromeda.id("pouches/saplings")), Main.SAPLING_POUCH),
        FLOWER(2, RegistryKey.of(RegistryKeys.LOOT_TABLE, Andromeda.id("pouches/flowers")), Main.FLOWER_POUCH),
        CUSTOM(3, null, Main.SPECIAL_POUCH) {
            @Override
            public @NotNull RegistryKey<LootTable> getLootId(ItemStack stack) {
                return stack.getOrDefault(Main.CUSTOM_COMPONENT.get(), CustomPouchComponent.DEFAULT).key();
            }
        };

        private static final Int2ObjectMap<Type> LOOKUP = Utilities.supply(() -> {
            Int2ObjectMap<Type> map = new Int2ObjectOpenHashMap<>();
            for (Type value : Type.values()) {
                map.put(value.syncId, value);
            }
            return map;
        });

        private final int syncId;
        @Nullable private final RegistryKey<LootTable> lootId;
        private final Keeper<PouchItem> defaultItem;

        Type(int syncId, @Nullable RegistryKey<LootTable> lootId, Keeper<PouchItem> defaultItem) {
            this.syncId = syncId;
            this.lootId = lootId;
            this.defaultItem = defaultItem;
        }

        public @NotNull RegistryKey<LootTable> getLootId(ItemStack stack) {
            return Objects.requireNonNull(lootId);
        }

        public PouchItem getDefaultItem() {
            return defaultItem.orThrow();
        }

        public static Type getType(int syncId) {
            return LOOKUP.get(syncId);
        }
    }
}
