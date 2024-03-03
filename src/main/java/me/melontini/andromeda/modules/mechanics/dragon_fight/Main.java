package me.melontini.andromeda.modules.mechanics.dragon_fight;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.World;

import java.util.Collections;

import static me.melontini.andromeda.common.registries.Common.id;

public class Main {
    Main() {
        EnderDragonManager.ATTACHMENT.init(AttachmentRegistry.<EnderDragonManager>builder()
                .initializer(() -> new EnderDragonManager(1, Collections.emptyList()))
                .persistent(EnderDragonManager.CODEC)
                .buildAndRegister(id("ender_dragon_data")));

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.END) world.getAttachedOrCreate(EnderDragonManager.ATTACHMENT.get());
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() == World.END)
                world.getAttachedOrCreate(EnderDragonManager.ATTACHMENT.get()).tick(world);
        });
    }
}
