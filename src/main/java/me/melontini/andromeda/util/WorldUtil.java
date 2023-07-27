package me.melontini.andromeda.util;

import me.melontini.andromeda.content.managers.CustomTraderManager;
import me.melontini.andromeda.content.managers.EnderDragonManager;
import me.melontini.andromeda.networks.AndromedaPackets;
import me.melontini.dark_matter.content.data.NbtBuilder;
import me.melontini.dark_matter.minecraft.world.PlayerUtil;
import me.melontini.dark_matter.util.MakeSure;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static me.melontini.andromeda.Andromeda.MODID;

public class WorldUtil {
    public static final Identifier BEE_LOOT_ID = new Identifier(MODID, "bee_nest/bee_nest_broken");

    public static final List<Direction> AROUND_BLOCK_DIRECTIONS = List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public static CustomTraderManager getTraderManager(@NotNull ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(nbtCompound -> {
            CustomTraderManager manager = new CustomTraderManager();
            manager.readNbt(nbtCompound);
            return manager;
        }, CustomTraderManager::new, "andromeda_trader_statemanager");
    }

    public static EnderDragonManager getEnderDragonManager(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(nbtCompound -> {
            EnderDragonManager manager = new EnderDragonManager(world);
            manager.readNbt(nbtCompound);
            return manager;
        }, () -> new EnderDragonManager(world), "andromeda_ender_dragon_fight");
    }

    public static void addParticle(World world, ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (!world.isClient) {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            packetByteBuf.writeRegistryValue(Registry.PARTICLE_TYPE, parameters.getType());
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
            var pos = new BlockPos(blockPos.getX() + random.nextBetween(-range, range), blockPos.getY() + random.nextBetween(-range, range), blockPos.getZ() + random.nextBetween(-range, range));
            if (world.getBlockState(pos.up()).isAir() && world.getBlockState(pos).isAir() && isClear(world, pos) && isClear(world, pos.up())) {
                return Optional.of(pos);
            }
        }
    }
}
