package me.melontini.andromeda.modules.items.lockpick.client;

import me.melontini.andromeda.modules.items.lockpick.MerchantInventoryScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class Client {

    Client() {
        MerchantInventoryScreenHandler.INSTANCE.ifPresent(s -> HandledScreens.register(s, MerchantInventoryScreen::new));
    }
}
