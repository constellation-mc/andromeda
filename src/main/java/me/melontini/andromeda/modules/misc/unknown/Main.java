package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Rarity;

import static me.melontini.andromeda.common.Andromeda.id;

public class Main {

    public static final Keeper<FlowerBlock> ROSE_OF_THE_VALLEY_BLOCK = Keeper.create();
    public static final Keeper<RoseOfTheValley> ROSE_OF_THE_VALLEY = Keeper.create();

    Main() {
        ROSE_OF_THE_VALLEY_BLOCK.init(RegistryUtil.register(CommonRegistries.blocks(), id("rose_of_the_valley"), () -> new FlowerBlock(StatusEffects.REGENERATION, 12, AbstractBlock.Settings.copy(Blocks.LILY_OF_THE_VALLEY))));
        ROSE_OF_THE_VALLEY.init(RegistryUtil.register(CommonRegistries.items(), id("rose_of_the_valley"), () -> new RoseOfTheValley(ROSE_OF_THE_VALLEY_BLOCK.orThrow(), new FabricItemSettings().rarity(Rarity.UNCOMMON))));
    }
}
