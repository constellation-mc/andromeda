package me.melontini.andromeda.common.util;

import lombok.NonNull;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.data.nbt.NbtBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class WorldUtil {
    public static final Identifier BEE_LOOT_ID = new Identifier(MODID, "bee_nest/bee_nest_broken");

    public static final List<Direction> AROUND_BLOCK_DIRECTIONS = List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public static void crudeSetVelocity(Entity entity, double x, double y, double z) {
        crudeSetVelocity(entity, new Vec3d(x, y, z));
    }

    public static void crudeSetVelocity(Entity entity, Vec3d velocity) {
        if (!entity.world.isClient) {
            entity.setVelocity(velocity);
            for (ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
            }
        } else {
            throw new UnsupportedOperationException("Can't send packets to client unless you're on server.");
        }
    }

    public static List<ItemStack> prepareLoot(@NonNull World world, @NonNull Identifier lootId) {
        return ((ServerWorld) world).getServer()
                .getLootManager()
                .getLootTable(lootId)
                .generateLoot(new LootContextParameterSet.Builder(((ServerWorld) world)).build(LootContextTypes.EMPTY));
    }

    public static void trySpawnFallingBeeNest(@NonNull World world, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull BeehiveBlockEntity beehiveBlockEntity) {
        FallingBlockEntity fallingBlock = new FallingBlockEntity(
                world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                state.contains(Properties.WATERLOGGED) ? state.with(Properties.WATERLOGGED, Boolean.FALSE) : state);

        //Thanks AccessWidener!
        fallingBlock.readCustomDataFromNbt(NbtBuilder.create()
                .put("TileEntityData", NbtBuilder.create()
                        .put("Bees", beehiveBlockEntity.getBees())
                        .putBoolean("AM-FromFallenBlock", true).build())
                .put("BlockState", NbtHelper.fromBlockState(state)).build());

        world.setBlockState(pos, state.getFluidState().getBlockState(), Block.NOTIFY_ALL);
        world.spawnEntity(fallingBlock);
    }

    public static boolean isClear(World world, BlockPos pos) {
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }
        for (Direction dir : AROUND_BLOCK_DIRECTIONS) {
            if (!world.getBlockState(pos.offset(dir)).isAir()) {
                return false;
            }
        }
        return true;
    }

    public static Optional<BlockPos> pickRandomSpot(@NonNull World world, @NonNull BlockPos blockPos, int range, @NonNull Random random) {
        MakeSure.isTrue(range > 0, "range can't be negative or zero!");
        double j = (range * range * range) * 0.75;

        while (j > 0) {
            j--;
            var pos = new BlockPos(blockPos.getX() + random.nextBetween(-range, range), blockPos.getY() + random.nextBetween(-range, range), blockPos.getZ() + random.nextBetween(-range, range));
            if (world.getBlockState(pos.up()).isAir() && world.getBlockState(pos).isAir() && isClear(world, pos) && isClear(world, pos.up())) {
                return Optional.of(pos);
            }
        }
        return Optional.empty();
    }
}
