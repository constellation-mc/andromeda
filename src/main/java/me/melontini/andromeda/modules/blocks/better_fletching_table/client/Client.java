package me.melontini.andromeda.modules.blocks.better_fletching_table.client;

import me.melontini.andromeda.modules.blocks.better_fletching_table.Main;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;

public class Client {

    Client() {
        Main.FLETCHING.ifPresent(s -> HandledScreens.register(s, FletchingScreen::new));

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null) return;

            int i = nbt.getInt("AM-Tightened");
            if (i > 0) lines.add(TextUtil.translatable("tooltip.andromeda.bow.tight", i).formatted(Formatting.GRAY));
        });
    }
}
