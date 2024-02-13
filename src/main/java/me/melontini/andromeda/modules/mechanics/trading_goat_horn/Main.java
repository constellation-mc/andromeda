package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.minecraft.world.PersistentStateHelper;
import me.melontini.dark_matter.api.minecraft.world.interfaces.TickableState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class Main {
    Main() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) CustomTraderManager.get(world);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PersistentStateHelper.consumeIfLoaded(MakeSure.notNull(server.getWorld(World.OVERWORLD)), CustomTraderManager.ID,
                    (world1, s) -> CustomTraderManager.get(world1), PersistentState::markDirty);
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() == World.OVERWORLD) PersistentStateHelper.consumeIfLoaded(world, CustomTraderManager.ID,
                    (world1, s) -> CustomTraderManager.get(world1), TickableState::tick);
        });
    }
}
