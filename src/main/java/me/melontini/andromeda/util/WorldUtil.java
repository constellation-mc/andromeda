package me.melontini.andromeda.util;

import me.melontini.andromeda.networks.AndromedaPackets;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.minecraft.data.NbtBuilder;
import me.melontini.dark_matter.api.minecraft.world.PlayerUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static me.melontini.andromeda.util.SharedConstants.MODID;

public class WorldUtil {
    public static final Identifier BEE_LOOT_ID = new Identifier(MODID, "bee_nest/bee_nest_broken");

    public static final List<Direction> AROUND_BLOCK_DIRECTIONS = List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public static void addParticle(World world, ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (!world.isClient) {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            packetByteBuf.writeIdentifier(Registry.PARTICLE_TYPE.getId(parameters.getType()));
            packetByteBuf.writeDouble(x);
            packetByteBuf.writeDouble(y);
            packetByteBuf.writeDouble(z);
            packetByteBuf.writeDouble(velocityX);
            packetByteBuf.writeDouble(velocityY);
            packetByteBuf.writeDouble(velocityZ);

            for (PlayerEntity player : PlayerUtil.findPlayersInRange(world, new BlockPos(x, y, z), 85)) {
                ServerPlayNetworking.send((ServerPlayerEntity) player, AndromedaPackets.ADD_ONE_PARTICLE, packetByteBuf);
            }
        } else {
            throw new UnsupportedOperationException("Can't send packets to client unless you're on server.");
        }
    }

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

    public static List<ItemStack> prepareLoot(@NotNull World world, Identifier lootId) {
        MakeSure.notNulls(world, lootId);
        return ((ServerWorld) world).getServer()
                .getLootManager()
                .getTable(lootId)
                .generateLoot((new LootContext.Builder((ServerWorld) world))
                        .random(world.random)
                        .build(LootContextTypes.EMPTY));
    }

    public static void trySpawnFallingBeeNest(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull BeehiveBlockEntity beehiveBlockEntity) {
        MakeSure.notNulls(world, pos, state, beehiveBlockEntity);
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

    public static Optional<BlockPos> pickRandomSpot(World world, BlockPos blockPos, int range, Random random) {
        MakeSure.notNulls(world, blockPos, random);
        MakeSure.isTrue(range > 0, "range can't be negative or zero!");
        int i = 0;
        double j = (range * range * range) * 0.75;
        while (true) {
            ++i;
            if (i > j) {
                return Optional.empty();
            }
            var pos = new BlockPos(blockPos.getX() + MathStuff.nextInt(random, -range, range), blockPos.getY() + MathStuff.nextInt(random, -range, range), blockPos.getZ() + MathStuff.nextInt(random, -range, range));
            if (world.getBlockState(pos.up()).isAir() && world.getBlockState(pos).isAir() && isClear(world, pos) && isClear(world, pos.up())) {
                return Optional.of(pos);
            }
        }
    }
}
