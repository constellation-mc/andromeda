package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;

import java.util.Set;

import static me.melontini.andromeda.common.Andromeda.id;

public class Main {

    public static final Keeper<IncubatorBlock> INCUBATOR_BLOCK = Keeper.create();
    public static final Keeper<BlockItem> INCUBATOR = Keeper.create();
    public static final Keeper<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BLOCK_ENTITY = Keeper.create();

    Main(Incubator module) {
        INCUBATOR_BLOCK.init(RegistryUtil.register(Registries.BLOCK, id("incubator"), () -> new IncubatorBlock(FabricBlockSettings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD))));
        INCUBATOR.init(RegistryUtil.register(Registries.ITEM, id("incubator"), () -> new BlockItem(INCUBATOR_BLOCK.orThrow(), new FabricItemSettings())));
        INCUBATOR_BLOCK_ENTITY.init(RegistryUtil.register(Registries.BLOCK_ENTITY_TYPE, id("incubator"), () -> new BlockEntityType<>(IncubatorBlockEntity::new, Set.of(INCUBATOR_BLOCK.orThrow()), null)));

        AndromedaItemGroup.accept(acceptor -> acceptor.keeper(module, ItemGroups.FUNCTIONAL, INCUBATOR));

        EggProcessingData.init();
    }
}
