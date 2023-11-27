package me.melontini.andromeda.modules.blocks.better_fletching_table.client;

import me.melontini.andromeda.modules.blocks.better_fletching_table.Content;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class Client {

    public static void init() {
        Content.FLETCHING.ifPresent(s -> HandledScreens.register(s, FletchingScreen::new));
    }
}
