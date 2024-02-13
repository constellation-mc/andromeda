package me.melontini.andromeda.modules.mechanics.dragon_fight;

import me.melontini.dark_matter.api.minecraft.world.PersistentStateHelper;
import me.melontini.dark_matter.api.minecraft.world.interfaces.TickableState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.World;

public class Main {
    Main() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.END) EnderDragonManager.get(world);
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() == World.END) PersistentStateHelper.consumeIfLoaded(world, EnderDragonManager.ID,
                    (world1, s) -> EnderDragonManager.get(world1), TickableState::tick);
        });
    }
}
