package dev.zenfyr.andromeda.modules.items.pouches.entities;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.items.pouches.PouchesMain;
import dev.zenfyr.andromeda.modules.items.pouches.items.PouchItem;
import dev.zenfyr.pulsar.api.itemstack.ItemStackUtil;
import dev.zenfyr.pulsar.api.util.Utilities;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PouchEntity extends ThrowableItemProjectile {

  private static final EntityDataAccessor<Integer> POUCH_TYPE =
      SynchedEntityData.defineId(PouchEntity.class, EntityDataSerializers.INT);

  public PouchEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
    super(entityType, level);
  }

  @Override
  protected void onHit(HitResult hitResult) {
    HitResult.Type type = hitResult.getType();

    ItemStack stack = getItem();
    if (type == HitResult.Type.ENTITY) {
      this.onHitEntity((EntityHitResult) hitResult);
      if (level() instanceof ServerLevel sw && !stack.isEmpty()) {
        sw.sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)),
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
      if (level() instanceof ServerLevel sw && !stack.isEmpty()) {
        sw.sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)),
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
    if (!level().isClientSide()) {
      var stacks = MiscUtil.prepareLoot(level(), this.getPouchType().getLootId(getItem()));

      Entity entity = entityHitResult.getEntity();
      if (entity instanceof Player pe) {
        stacks.forEach(stack -> pe.getInventory().placeItemBackInInventory(stack));
        return;
      } else if (entity instanceof InventoryCarrier io) {
        var storage = ContainerStorage.of(io.getInventory(), null);
        stacks.forEach(
            stack -> PouchesMain.tryInsertItem(level(), this.position(), stack, storage));
        return;
      } else if (entity instanceof Container inv) {
        var storage = ContainerStorage.of(inv, null);
        stacks.forEach(
            stack -> PouchesMain.tryInsertItem(level(), this.position(), stack, storage));
        return;
      }
      stacks.forEach(stack -> ItemStackUtil.spawnVelocity(
          this.position(), stack, level(), -0.2, 0.2, 0.1, 0.2, -0.2, 0.2));
    }
  }

  @Override
  protected void onHitBlock(BlockHitResult blockHitResult) {
    if (!level().isClientSide()) {
      var stacks = MiscUtil.prepareLoot(level(), this.getPouchType().getLootId(getItem()));

      var be = level().getBlockEntity(blockHitResult.getBlockPos());
      if ((be != null && PouchesMain.getViewCount(be) > 0)) {
        var storage = ItemStorage.SIDED.find(
            level(),
            blockHitResult.getBlockPos(),
            level().getBlockState(blockHitResult.getBlockPos()),
            be,
            blockHitResult.getDirection());
        if (storage != null) {
          stacks.forEach(
              stack -> PouchesMain.tryInsertItem(level(), this.position(), stack, storage));
          return;
        }
      }
      stacks.forEach(stack -> ItemStackUtil.spawnVelocity(
          this.position(), stack, level(), -0.2, 0.2, 0.1, 0.2, -0.2, 0.2));
    }
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(POUCH_TYPE, Type.SEED.syncId);
  }

  @Override
  protected PouchItem getDefaultItem() {
    return Type.SEED.getDefaultItem();
  }

  public Type getPouchType() {
    return Type.getType(this.entityData.get(POUCH_TYPE));
  }

  public void setPouchType(Type type) {
    this.entityData.set(POUCH_TYPE, type.syncId);
  }

  @Override
  public void addAdditionalSaveData(ValueOutput nbt) {
    nbt.putString("Type", getPouchType().name());
  }

  @Override
  public void readAdditionalSaveData(ValueInput nbt) {
    if (nbt.contains("Type")) {
      setPouchType(Type.valueOf(nbt.getStringOr("Type", "")));
    }
  }

  public enum Type {
    SEED(0, Andromeda.key(Registries.LOOT_TABLE, "pouches/seeds"), PouchesMain.SEED_POUCH),
    SAPLING(1, Andromeda.key(Registries.LOOT_TABLE, "pouches/saplings"), PouchesMain.SAPLING_POUCH),
    FLOWER(2, Andromeda.key(Registries.LOOT_TABLE, "pouches/flowers"), PouchesMain.FLOWER_POUCH),
    CUSTOM(3, null, PouchesMain.SPECIAL_POUCH) {
      @Override
      public @NotNull ResourceKey<LootTable> getLootId(ItemStack stack) {
        return stack
            .getOrDefault(PouchesMain.CUSTOM_COMPONENT.get(), CustomPouchComponent.DEFAULT)
            .key();
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

    @Nullable private final ResourceKey<LootTable> lootId;

    private final Keeper<PouchItem> defaultItem;

    Type(int syncId, @Nullable ResourceKey<LootTable> lootId, Keeper<PouchItem> defaultItem) {
      this.syncId = syncId;
      this.lootId = lootId;
      this.defaultItem = defaultItem;
    }

    public @NotNull ResourceKey<LootTable> getLootId(ItemStack stack) {
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
