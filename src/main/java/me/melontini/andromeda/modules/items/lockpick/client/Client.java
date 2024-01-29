package me.melontini.andromeda.modules.items.lockpick.client;

import me.melontini.andromeda.modules.items.lockpick.Main;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class Client {

    Client() {
        Main.MERCHANT_INVENTORY.ifPresent(s -> HandledScreens.register(s, MerchantInventoryScreen::new));
    }
}
