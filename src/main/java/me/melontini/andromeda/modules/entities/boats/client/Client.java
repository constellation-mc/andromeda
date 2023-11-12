package me.melontini.andromeda.modules.entities.boats.client;

import me.melontini.andromeda.modules.entities.boats.BoatEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.util.math.Direction;

public class Client {

    public static void init() {
        BoatEntities.BOAT_WITH_FURNACE.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, Direction.NORTH))));
        BoatEntities.BOAT_WITH_JUKEBOX.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.JUKEBOX.getDefaultState())));
        BoatEntities.BOAT_WITH_TNT.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.TNT.getDefaultState())));
        BoatEntities.BOAT_WITH_HOPPER.ifPresent(e -> EntityRendererRegistry.register(e, ctx -> new BoatWithBlockRenderer(ctx, Blocks.HOPPER.getDefaultState())));
    }
}
