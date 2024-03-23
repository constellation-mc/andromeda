package me.melontini.andromeda.modules.items.pouches.entities;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.common.util.ItemStackUtil;
import me.melontini.andromeda.common.util.WorldUtil;
import me.melontini.andromeda.modules.items.pouches.Main;
import me.melontini.andromeda.modules.items.pouches.items.PouchItem;
import me.melontini.dark_matter.api.base.util.Utilities;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class PouchEntity extends ThrownItemEntity {

    private static final TrackedData<Integer> POUCH_TYPE = DataTracker.registerData(PouchEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public PouchEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public PouchEntity(double d, double e, double f, World world) {
        super(Main.POUCH.orThrow(), d, e, f, world);
    }

    public PouchEntity(LivingEntity livingEntity, World world) {
        super(Main.POUCH.orThrow(), livingEntity, world);
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
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(POUCH_TYPE, Type.SEED.ordinal());
    }

    @Override
    protected PouchItem getDefaultItem() {
        return getPouchType().getDefaultItem();
    }

    public Type getPouchType() {
        return Type.getType(this.dataTracker.get(POUCH_TYPE));
    }

    public void setPouchType(Type type) {
        this.dataTracker.set(POUCH_TYPE, type.ordinal());
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
        SEED(new Identifier(MODID, "pouches/seeds"), Main.SEED_POUCH),
        SAPLING(new Identifier(MODID, "pouches/saplings"), Main.SAPLING_POUCH),
        FLOWER(new Identifier(MODID, "pouches/flowers"), Main.FLOWER_POUCH),
        CUSTOM(null, Main.SPECIAL_POUCH) {
            @Override
            public Identifier getLootId(ItemStack stack) {
                NbtCompound nbt = stack.getNbt();
                if (nbt != null && nbt.contains("CustomLootId")) {
                    return new Identifier(nbt.getString("CustomLootId"));
                }
                return new Identifier(MODID, "pouches/seeds");
            }
        };

        private static final Int2ObjectMap<Type> LOOKUP = Utilities.supply(() -> {
            Int2ObjectMap<Type> map = new Int2ObjectOpenHashMap<>();
            for (Type value : Type.values()) {
                map.put(value.ordinal(), value);
            }
            return map;
        });

        private final Identifier lootId;
        private final Keeper<PouchItem> defaultItem;

        Type(Identifier lootId, Keeper<PouchItem> defaultItem) {
            this.lootId = lootId;
            this.defaultItem = defaultItem;
        }

        public Identifier getLootId(ItemStack stack) {
            return lootId;
        }

        public PouchItem getDefaultItem() {
            return defaultItem.orThrow();
        }

        public static Type getType(int ordinal) {
            return LOOKUP.get(ordinal);
        }
    }
}
