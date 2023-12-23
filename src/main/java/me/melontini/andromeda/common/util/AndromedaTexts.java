package me.melontini.andromeda.common.util;

import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AndromedaTexts {
    public static final Text ITEM_GROUP_NAME = TextUtil.translatable("itemGroup.andromeda.items");
    public static final Text FLETCHING_SCREEN = TextUtil.translatable("gui.andromeda.fletching");
    public static final Text SAFE_BEDS = TextUtil.translatable("action.andromeda.safebeds");
    public static final Text INCUBATOR_SECRET = TextUtil.translatable("tooltip.andromeda.incubator[1]").formatted(Formatting.GRAY);
    public static final Text ITEM_IN_FRAME = TextUtil.translatable("tooltip.andromeda.frameitem").formatted(Formatting.GRAY);
    public static final Text ROSE_OF_THE_VALLEY_TOOLTIP = TextUtil.translatable("tooltip.andromeda.rose_of_the_valley").formatted(Formatting.GRAY);
}
