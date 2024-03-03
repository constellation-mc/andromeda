package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.World;

public class Main {
    Main() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) world.getAttachedOrCreate(CustomTraderManager.ATTACHMENT);
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() == World.OVERWORLD) world.getAttachedOrCreate(CustomTraderManager.ATTACHMENT).tick();
        });
    }
}
