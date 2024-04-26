package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.World;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {
    Main() {
        CustomTraderManager.ATTACHMENT.init(AttachmentRegistry.<CustomTraderManager>builder()
                .initializer(() -> new CustomTraderManager(0))
                .persistent(CustomTraderManager.CODEC)
                .buildAndRegister(id("trader_state_manager")));

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (World.OVERWORLD.equals(world.getRegistryKey())) world.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get());
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (World.OVERWORLD.equals(world.getRegistryKey())) world.getAttachedOrCreate(CustomTraderManager.ATTACHMENT.get()).tick();
        });
    }
}
