package me.melontini.andromeda.modules.world.loot_barrels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Arrays;
import java.util.Comparator;

public class LootBarrelFeature extends Feature<LootBarrelFeature.LootBarrelConfiguration> {

    private static final Direction[] HORIZONTAL = Arrays.stream(Direction.values()).filter((direction) -> direction.getAxis().isHorizontal()).sorted(Comparator.comparingInt(Direction::getHorizontal)).toArray(Direction[]::new);

    public LootBarrelFeature() {
        super(LootBarrelConfiguration.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<LootBarrelConfiguration> context) {
        var world = context.getWorld();
        if (!world.getBlockState(context.getOrigin()).isAir() || !world.getBlockState(context.getOrigin().down()).isOpaque()) return false;

        world.setBlockState(context.getOrigin(),
                Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Util.getRandom(HORIZONTAL, context.getRandom())),
                Block.NOTIFY_LISTENERS);
        LootableContainerBlockEntity.setLootTable(world, context.getRandom(), context.getOrigin(), context.getConfig().loot());

        if (world.getBlockState(context.getOrigin().up()).isAir() && context.getConfig().decorations().stream().findAny().isPresent()) {
            world.setBlockState(context.getOrigin().up(),
                    context.getConfig().decorations().shuffle().stream().findFirst().orElseThrow(),
                    Block.NOTIFY_LISTENERS);
        }
        return true;
    }

    public record LootBarrelConfiguration(Identifier loot, WeightedList<BlockState> decorations) implements FeatureConfig {

        public static final Codec<LootBarrelConfiguration> CODEC = RecordCodecBuilder.create(data -> data.group(
                Identifier.CODEC.fieldOf("loot").forGetter(LootBarrelConfiguration::loot),
                WeightedList.createCodec(BlockState.CODEC).fieldOf("decorations").forGetter(LootBarrelConfiguration::decorations)
        ).apply(data, LootBarrelConfiguration::new));
    }

    public static void init() {
        Registry.register(Registries.FEATURE, Andromeda.id("loot_barrel"), new LootBarrelFeature());

        RegistryKey<PlacedFeature> key = Andromeda.key(RegistryKeys.PLACED_FEATURE, "loot_barrel");
        BiomeModifications.addFeature(context -> context.canGenerateIn(DimensionOptions.OVERWORLD), GenerationStep.Feature.UNDERGROUND_STRUCTURES, key);
    }
}
