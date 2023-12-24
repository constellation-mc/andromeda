package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.conflicts.CommonItemGroups;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.sound.BlockSoundGroup;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.common.registries.Common.start;
import static me.melontini.dark_matter.api.content.RegistryUtil.asItem;

public class Content {

    public static final Keeper<IncubatorBlock> INCUBATOR_BLOCK = start(() -> ContentBuilder.BlockBuilder.create(id("incubator"), () -> new IncubatorBlock(FabricBlockSettings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(id, () -> new BlockItem(block, new FabricItemSettings())).itemGroup(CommonItemGroups.redstone()))
            .blockEntity((block, id) -> ContentBuilder.BlockEntityBuilder.create(id, IncubatorBlockEntity::new, block)))
            .afterInit(item -> AndromedaItemGroup.accept(acceptor -> acceptor.item(ModuleManager.quick(Incubator.class), item)));

    public static final Keeper<BlockItem> INCUBATOR = Keeper.of(() -> asItem(INCUBATOR_BLOCK.get()));

    public static final Keeper<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY = Keeper.of(() ->
            INCUBATOR_BLOCK.get() == null ? null : RegistryUtil.asBlockEntity(INCUBATOR_BLOCK.get()));
}
