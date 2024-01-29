package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.content.ContentBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Rarity;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.dark_matter.api.content.RegistryUtil.asItem;

public class Main {

    public static final Keeper<FlowerBlock> ROSE_OF_THE_VALLEY_BLOCK = Keeper.create();
    public static final Keeper<RoseOfTheValley> ROSE_OF_THE_VALLEY = Keeper.create();

    public static String DEBUG_SPLASH;

    Main() {
        ROSE_OF_THE_VALLEY_BLOCK.init(ContentBuilder.BlockBuilder.create(id("rose_of_the_valley"), () -> new FlowerBlock(StatusEffects.REGENERATION, 12, AbstractBlock.Settings.copy(Blocks.LILY_OF_THE_VALLEY)))
                .item((block, id) -> ContentBuilder.ItemBuilder.create(id, () -> new RoseOfTheValley(block, new FabricItemSettings().rarity(Rarity.UNCOMMON)))).build());
        ROSE_OF_THE_VALLEY.init(asItem(ROSE_OF_THE_VALLEY_BLOCK.orThrow()));
    }
}
