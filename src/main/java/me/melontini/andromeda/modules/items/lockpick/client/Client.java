package me.melontini.andromeda.modules.items.lockpick.client;

import me.melontini.andromeda.modules.items.lockpick.Content;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class Client {

    public static void init() {
        Content.MERCHANT_INVENTORY.ifPresent(s -> HandledScreens.register(s, MerchantInventoryScreen::new));
    }
}
