package dev.zenfyr.andromeda.modules.items.pouches.entities;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.common.util.LootContextBuilder;
import dev.zenfyr.andromeda.modules.items.pouches.Main;
import dev.zenfyr.andromeda.modules.items.pouches.items.PouchItem;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.minecraft.util.ItemStackUtil;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PouchEntity extends ThrowableItemProjectile {

  private static final EntityDataAccessor<Integer> POUCH_TYPE =
      SynchedEntityData.defineId(PouchEntity.class, EntityDataSerializers.INT);

  public PouchEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
    super(entityType, world);
  }

  public PouchEntity(double d, double e, double f, Level world) {
    super(Main.POUCH.orThrow(), d, e, f, world);
  }

  public PouchEntity(LivingEntity livingEntity, Level world) {
    super(Main.POUCH.orThrow(), livingEntity, world);
  }

  @Override
  protected void onHit(HitResult hitResult) {
    HitResult.Type type = hitResult.getType();

    ItemStack stack = getItem();
    if (type == HitResult.Type.ENTITY) {
      this.onHitEntity((EntityHitResult) hitResult);
      if (level instanceof ServerLevel sw) {
        sw.sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, stack),
            getX(),
            getY(),
            getZ(),
            10,
            0.2,
            0.2,
            0.2,
            0.25);
      }
      this.discard();
    } else if (type == HitResult.Type.BLOCK) {
      this.onHitBlock((BlockHitResult) hitResult);
      if (level instanceof ServerLevel sw) {
        sw.sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, stack),
            getX(),
            getY(),
            getZ(),
            10,
            0.2,
            0.2,
            0.2,
            0.25);
      }
      this.discard();
    }

    if (type != HitResult.Type.MISS) {
      this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
    }
  }

  @Override
  protected void onHitEntity(EntityHitResult entityHitResult) {
    if (!level.isClientSide()) {
      var stacks = LootContextBuilder.prepareLoot(level, this.getPouchType().getLootId(getItem()));

      Entity entity = entityHitResult.getEntity();
      if (entity instanceof Player pe) {
        stacks.forEach(stack -> pe.getInventory().placeItemBackInInventory(stack));
        return;
      } else if (entity instanceof InventoryCarrier io) {
        var storage = InventoryStorage.of(io.getInventory(), null);
        stacks.forEach(stack -> Main.tryInsertItem(level, this.position(), stack, storage));
        return;
      } else if (entity instanceof Container inv) {
        var storage = InventoryStorage.of(inv, null);
        stacks.forEach(stack -> Main.tryInsertItem(level, this.position(), stack, storage));
        return;
      }
      stacks.forEach(stack -> ItemStackUtil.spawnVelocity(
          this.position(), stack, level, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2));
    }
  }

  @Override
  protected void onHitBlock(BlockHitResult blockHitResult) {
    if (!level.isClientSide()) {
      var stacks = LootContextBuilder.prepareLoot(level, this.getPouchType().getLootId(getItem()));

      var be = level.getBlockEntity(blockHitResult.getBlockPos());
      if ((be != null && Main.getViewCount(be) > 0)) {
        var storage = ItemStorage.SIDED.find(
            level,
            blockHitResult.getBlockPos(),
            level.getBlockState(blockHitResult.getBlockPos()),
            be,
            blockHitResult.getDirection());
        if (storage != null) {
          stacks.forEach(stack -> Main.tryInsertItem(level, this.position(), stack, storage));
          return;
        }
      }
      stacks.forEach(stack -> ItemStackUtil.spawnVelocity(
          this.position(), stack, level, -0.2, 0.2, 0.1, 0.2, -0.2, 0.2));
    }
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(POUCH_TYPE, Type.SEED.syncId);
  }

  @Override
  protected PouchItem getDefaultItem() {
    return getPouchType().getDefaultItem();
  }

  public Type getPouchType() {
    return Type.getType(this.entityData.get(POUCH_TYPE));
  }

  public void setPouchType(Type type) {
    this.entityData.set(POUCH_TYPE, type.syncId);
  }

  @Override
  public void addAdditionalSaveData(CompoundTag nbt) {
    nbt.putString("Type", getPouchType().name());
  }

  @Override
  public void readAdditionalSaveData(CompoundTag nbt) {
    if (nbt.contains("Type")) {
      setPouchType(Type.valueOf(nbt.getString("Type")));
    }
  }

  public enum Type {
    SEED(0, Andromeda.id("pouches/seeds"), Main.SEED_POUCH),
    SAPLING(1, Andromeda.id("pouches/saplings"), Main.SAPLING_POUCH),
    FLOWER(2, Andromeda.id("pouches/flowers"), Main.FLOWER_POUCH),
    CUSTOM(3, null, Main.SPECIAL_POUCH) {
      @Override
      public @NotNull ResourceLocation getLootId(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("CustomLootId")) {
          return new ResourceLocation(nbt.getString("CustomLootId"));
        }
        return SEED.getLootId(stack);
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

    @Nullable private final ResourceLocation lootId;

    private final Keeper<PouchItem> defaultItem;

    Type(int syncId, @Nullable ResourceLocation lootId, Keeper<PouchItem> defaultItem) {
      this.syncId = syncId;
      this.lootId = lootId;
      this.defaultItem = defaultItem;
    }

    public @NotNull ResourceLocation getLootId(ItemStack stack) {
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
