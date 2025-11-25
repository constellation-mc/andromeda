package dev.zenfyr.andromeda.modules.world.loot_barrels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Comparator;
import dev.zenfyr.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

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

    world.setBlock(
        context.origin(),
        Blocks.BARREL
            .defaultBlockState()
            .setValue(BarrelBlock.FACING, Util.getRandom(HORIZONTAL, context.random())),
        Block.UPDATE_CLIENTS);
    RandomizableContainerBlockEntity.setLootTable(
        world, context.random(), context.origin(), context.config().loot());

    if (world.getBlockState(context.origin().above()).isAir()
        && context.config().decorations().stream().findAny().isPresent()) {
      world.setBlock(
          context.origin().above(),
          context.config().decorations().shuffle().stream().findFirst().orElseThrow(),
          Block.UPDATE_CLIENTS);
    }
    return true;
  }

  public record LootBarrelConfiguration(
      ResourceLocation loot, ShufflingList<BlockState> decorations)
      implements FeatureConfiguration {

    public static final Codec<LootBarrelConfiguration> CODEC =
        RecordCodecBuilder.create(data -> data.group(
                ResourceLocation.CODEC.fieldOf("loot").forGetter(LootBarrelConfiguration::loot),
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
