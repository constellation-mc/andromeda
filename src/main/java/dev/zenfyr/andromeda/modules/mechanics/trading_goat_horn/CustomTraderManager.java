package dev.zenfyr.andromeda.modules.mechanics.trading_goat_horn;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.api.util.MathUtil;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class CustomTraderManager {

  public static final Codec<CustomTraderManager> CODEC = RecordCodecBuilder.create(
      data -> data.group(Codec.INT.fieldOf("cooldown").forGetter(CustomTraderManager::getCooldown))
          .apply(data, CustomTraderManager::new));

  public static final Keeper<AttachmentType<CustomTraderManager>> ATTACHMENT = Keeper.create();
  public static final TagKey<Instrument> TRADER_SONGS =
      TagKey.create(Registries.INSTRUMENT, id("trader_songs"));

  @Getter
  public int cooldown;

  private WanderingTrader trader;

  public CustomTraderManager(int cooldown) {
    this.cooldown = cooldown;
  }

  public void tick() {
    if (this.cooldown > 0) this.cooldown--;
    if (trader != null && trader.isRemoved()) trader = null;
  }

  public void trySpawn(
      ServerLevel level,
      ServerLevelData properties,
      ItemStack stackInHand,
      Player player,
      boolean highlight) {
    if (player == null) return;

    if (cooldown > 0) {
      if (!highlight || this.trader == null || this.trader.isRemoved()) return;

      this.trader.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 5, 0, true, false));
      return;
    }
    BlockPos blockPos = player.blockPosition();

    PoiManager pointOfInterestStorage = level.getPoiManager();
    Optional<BlockPos> optional = pointOfInterestStorage.find(
        registryEntry -> registryEntry.is(PoiTypes.MEETING),
        pos -> true,
        blockPos,
        48,
        PoiManager.Occupancy.ANY);
    BlockPos blockPos2 = optional.orElse(blockPos);
    BlockPos blockPos3 = getNearbySpawnPos(level, blockPos2, 48);

    if (blockPos3 == null || !doesNotSuffocateAt(level, blockPos3)) return;
    if (level.getBiome(blockPos3).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) return;

    WanderingTrader wanderingTraderEntity =
        EntityTypes.WANDERING_TRADER.spawn(level, blockPos3, EntitySpawnReason.EVENT);
    if (wanderingTraderEntity == null) return;
    this.trader = wanderingTraderEntity;

    var tCooldown = level.am$get(GoatHorn.CONFIG).cooldown;

    cooldown = tCooldown;
    for (int j = 0; j < 2; ++j) {
      spawnLlama(level, this.trader);
    }

    // properties.setWanderingTraderId(this.trader.getUUID());
    this.trader.setDespawnDelay(tCooldown);
    this.trader.setWanderTarget(blockPos2);
    this.trader.setHomeTo(blockPos2, 16);
    this.trader.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 8, 0, true, false));
  }

  private void spawnLlama(@NonNull ServerLevel level, @NonNull WanderingTrader wanderingTrader) {
    BlockPos blockPos = this.getNearbySpawnPos(level, wanderingTrader.blockPosition(), 4);
    if (blockPos == null) return;

    TraderLlama traderLlamaEntity =
        EntityTypes.TRADER_LLAMA.spawn(level, blockPos, EntitySpawnReason.EVENT);
    if (traderLlamaEntity == null) return;

    traderLlamaEntity.setLeashedTo(wanderingTrader, true);
  }

  @Nullable private BlockPos getNearbySpawnPos(LevelReader world, BlockPos pos, int range) {
    BlockPos blockPos = null;
    SpawnPlacementType placements = SpawnPlacements.getPlacementType(EntityTypes.WANDERING_TRADER);

    for (int i = 0; i < 10; ++i) {
      int x = pos.getX() + MathUtil.threadRandom().nextInt(range * 2) - range;
      int z = pos.getZ() + MathUtil.threadRandom().nextInt(range * 2) - range;
      int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
      BlockPos blockPos2 = new BlockPos(x, y, z);
      if (placements.isSpawnPositionOk(world, blockPos2, EntityTypes.WANDERING_TRADER)) {
        blockPos = blockPos2;
        break;
      }
    }

    return blockPos;
  }

  private boolean doesNotSuffocateAt(BlockGetter world, BlockPos pos) {
    for (BlockPos blockPos : BlockPos.betweenClosed(pos, pos.offset(1, 2, 1))) {
      if (!world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty()) {
        return false;
      }
    }

    return true;
  }

  static void init() {
    CustomTraderManager.ATTACHMENT.init(AttachmentRegistry.create(
        id("trader_state_manager"),
        builder -> builder
            .initializer(() -> new CustomTraderManager(0))
            .persistent(CustomTraderManager.CODEC)));

    ServerLevelEvents.LOAD.register((server, world) -> {
      if (Level.OVERWORLD.equals(world.dimension()))
        world.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get());
    });

    ServerTickEvents.END_LEVEL_TICK.register(world -> {
      if (Level.OVERWORLD.equals(world.dimension()))
        world.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get()).tick();
    });
  }
}
