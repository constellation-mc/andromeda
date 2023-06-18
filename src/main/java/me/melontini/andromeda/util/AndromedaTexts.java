package me.melontini.andromeda.util;

import me.melontini.crackerutil.util.TextUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AndromedaTexts {
    public static final Text ITEM_GROUP_NAME = TextUtil.translatable("itemGroup.andromeda.items");
    public static final MutableText MINECART_LINK_WHAT = TextUtil.translatable("andromeda.simpleMinecartLinking.what");
    public static final MutableText MINECART_LINK_TOO_FAR = TextUtil.translatable("andromeda.simpleMinecartLinking.too_far");
    public static final MutableText MINECART_LINK_SELF = TextUtil.translatable("andromeda.simpleMinecartLinking.link_self");
    public static final MutableText MINECART_LINK_DE_SYNC = TextUtil.translatable("andromeda.simpleMinecartLinking.de_sync");
    public static final Text FLETCHING_SCREEN = TextUtil.translatable("gui.andromeda.fletching");
    public static final Text SAFE_BEDS = TextUtil.translatable("andromeda.safebeds.action");
    public static final Text INCUBATOR_0 = TextUtil.translatable("tooltip.andromeda.incubator[0]");
    public static final Text INCUBATOR_1 = TextUtil.translatable("tooltip.andromeda.incubator[1]").formatted(Formatting.GRAY);
    public static final Text ITEM_IN_FRAME = TextUtil.translatable("tooltip.andromeda.frameitem").formatted(Formatting.GRAY);
    public static final Text ROSE_OF_THE_VALLEY_TOOLTIP = TextUtil.translatable("tooltip.andromeda.rose_of_the_valley").formatted(Formatting.GRAY);
}
