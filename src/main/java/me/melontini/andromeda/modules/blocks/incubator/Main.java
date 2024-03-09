package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.sound.BlockSoundGroup;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.dark_matter.api.content.RegistryUtil.asItem;

public class Main {

    public static final Keeper<IncubatorBlock> INCUBATOR_BLOCK = Keeper.create();
    public static final Keeper<BlockItem> INCUBATOR = Keeper.create();
    public static final Keeper<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY = Keeper.create();

    Main(Incubator module) {
        INCUBATOR_BLOCK.init(ContentBuilder.BlockBuilder.create(id("incubator"), () -> new IncubatorBlock(FabricBlockSettings.of(Material.WOOD).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)))
                .item((block, id) -> ContentBuilder.ItemBuilder.create(id, () -> new BlockItem(block, new FabricItemSettings())).itemGroup(CommonItemGroups.redstone()))
                .blockEntity((block, id) -> ContentBuilder.BlockEntityBuilder.create(id, IncubatorBlockEntity::new, block)).build());
        INCUBATOR.init(asItem(INCUBATOR_BLOCK.get()));
        INCUBATOR_BLOCK_ENTITY.init(!INCUBATOR_BLOCK.isPresent() ? null : RegistryUtil.asBlockEntity(INCUBATOR_BLOCK.get()));

        AndromedaItemGroup.accept(acceptor -> acceptor.keeper(module, INCUBATOR));

        EggProcessingData.init();
    }
}
