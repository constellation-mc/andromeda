package dev.zenfyr.andromeda.modules.world.loot_barrels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.common.Andromeda;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootBarrelFeature extends Feature<LootBarrelFeature.LootBarrelConfiguration> {

  private static final Direction[] HORIZONTAL = Arrays.stream(Direction.values())
      .filter((direction) -> direction.getAxis().isHorizontal())
      .sorted(Comparator.comparingInt(Direction::get2DDataValue))
      .toArray(Direction[]::new);

  public LootBarrelFeature() {
    super(LootBarrelConfiguration.CODEC);
  }

  @Override
  public boolean place(FeaturePlaceContext<LootBarrelConfiguration> context) {
    var world = context.level();
    if (!world.getBlockState(context.origin()).isAir()
        || !world.getBlockState(context.origin().below()).canOcclude()) return false;

    Predicate<BlockState> predicate = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
    this.safeSetBlock(
        world,
        context.origin(),
        Blocks.BARREL
            .defaultBlockState()
            .setValue(BarrelBlock.FACING, Util.getRandom(HORIZONTAL, context.random())),
        predicate);

    RandomizableContainer.setBlockEntityLootTable(
        world, context.random(), context.origin(), context.config().loot());

    if (world.getBlockState(context.origin().above()).isAir()
        && context.config().decorations().stream().findAny().isPresent()) {
      this.safeSetBlock(
          world,
          context.origin().above(),
          context.config().decorations().shuffle().stream().findFirst().orElseThrow(),
          predicate);
    }
    return true;
  }

  public record LootBarrelConfiguration(
      ResourceKey<LootTable> loot, ShufflingList<BlockState> decorations)
      implements FeatureConfiguration {

    public static final Codec<LootBarrelConfiguration> CODEC =
        RecordCodecBuilder.create(data -> data.group(
                ResourceKey.codec(Registries.LOOT_TABLE)
                    .fieldOf("loot")
                    .forGetter(LootBarrelConfiguration::loot),
                ShufflingList.codec(BlockState.CODEC)
                    .fieldOf("decorations")
                    .forGetter(LootBarrelConfiguration::decorations))
            .apply(data, LootBarrelConfiguration::new));
  }

  public static void init() {
    Registry.register(
        BuiltInRegistries.FEATURE, Andromeda.id("loot_barrel"), new LootBarrelFeature());

    ResourceKey<PlacedFeature> key = Andromeda.key(Registries.PLACED_FEATURE, "loot_barrel");
    BiomeModifications.addFeature(
        context -> context.canGenerateIn(LevelStem.OVERWORLD),
        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
        key);
  }
}
