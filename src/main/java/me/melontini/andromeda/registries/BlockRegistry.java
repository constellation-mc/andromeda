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
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import static me.melontini.andromeda.util.SharedConstants.MODID;

public class BlockRegistry {

    public static FlowerBlock ROSE_OF_THE_VALLEY = ContentBuilder.BlockBuilder.create(new Identifier(MODID, "rose_of_the_valley"), () -> new FlowerBlock(StatusEffects.REGENERATION, 12, AbstractBlock.Settings.copy(Blocks.LILY_OF_THE_VALLEY)))
            .item((block, identifier) -> ContentBuilder.ItemBuilder.create(identifier, () -> new RoseOfTheValley(block, new FabricItemSettings().rarity(Rarity.UNCOMMON))))
            .register(Config.get().unknown).build();

    public static IncubatorBlock INCUBATOR_BLOCK = ContentBuilder.BlockBuilder.create(new Identifier(MODID, "incubator"), () -> new IncubatorBlock(FabricBlockSettings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)))
            .item((block, identifier) -> ContentBuilder.ItemBuilder.create(identifier, () -> new BlockItem(block, new FabricItemSettings())).itemGroup(Registries.ITEM_GROUP.get(ItemGroups.REDSTONE)))
            .blockEntity((block, identifier) -> ContentBuilder.BlockEntityBuilder.create(identifier, IncubatorBlockEntity::new, block))
            .register(Config.get().incubatorSettings.enableIncubator).build();

    public static BlockEntityType<IncubatorBlockEntity> INCUBATOR_BLOCK_ENTITY = RegistryUtil.getBlockEntityFromBlock(INCUBATOR_BLOCK);

    public static void register() {
        AndromedaLog.info("BlockRegistry init complete!");
    }
}
