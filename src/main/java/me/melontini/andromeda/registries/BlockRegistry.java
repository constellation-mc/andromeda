package me.melontini.andromeda.registries;

import me.melontini.andromeda.blocks.IncubatorBlock;
import me.melontini.andromeda.blocks.entities.IncubatorBlockEntity;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.items.RoseOfTheValley;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;

import java.util.Objects;

import static me.melontini.andromeda.registries.Common.*;

public class BlockRegistry {

    private static BlockRegistry INSTANCE;

    public final FlowerBlock ROSE_OF_THE_VALLEY = ContentBuilder.BlockBuilder.create(id("rose_of_the_valley"), () -> new FlowerBlock(StatusEffects.REGENERATION, 12, AbstractBlock.Settings.copy(Blocks.LILY_OF_THE_VALLEY)))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(id, () -> new RoseOfTheValley(block, new FabricItemSettings().rarity(Rarity.UNCOMMON))))
            .register(Config.get().unknown).build();

    public final IncubatorBlock INCUBATOR_BLOCK = ContentBuilder.BlockBuilder.create(id("incubator"), () -> new IncubatorBlock(FabricBlockSettings.of(Material.WOOD).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(id, () -> new BlockItem(block, new FabricItemSettings())).itemGroup(call(() -> ItemGroup.REDSTONE)))
            .blockEntity((block, id) -> ContentBuilder.BlockEntityBuilder.create(id, IncubatorBlockEntity::new, block))
            .register(Config.get().incubator.enable).build();

    public final BlockEntityType<IncubatorBlockEntity> INCUBATOR_BLOCK_ENTITY = INCUBATOR_BLOCK == null ? null : RegistryUtil.asBlockEntity(INCUBATOR_BLOCK);

    public static BlockRegistry get() {
        return Objects.requireNonNull(INSTANCE, "%s requested too early!".formatted(INSTANCE.getClass()));
    }

    public static void register() {
        if (INSTANCE != null) throw new IllegalStateException("%s already initialized!".formatted(INSTANCE.getClass()));

        INSTANCE = new BlockRegistry();
        share("andromeda:block_registry", INSTANCE);
        AndromedaLog.info("%s init complete!".formatted(INSTANCE.getClass().getSimpleName()));
    }
}
