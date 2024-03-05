package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.WorldView;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class CustomTraderManager {

    public static final Codec<CustomTraderManager> CODEC = RecordCodecBuilder.create(data -> data.group(
            Codec.INT.fieldOf("cooldown").forGetter(CustomTraderManager::getCooldown)
    ).apply(data, CustomTraderManager::new));

    public static final Keeper<AttachmentType<CustomTraderManager>> ATTACHMENT = Keeper.create();

    @Getter
    public int cooldown;

    public CustomTraderManager(int cooldown) {
        this.cooldown = cooldown;
    }

    public void tick() {
        if (this.cooldown > 0) this.cooldown--;
    }

    public void trySpawn(ServerWorld world, ServerWorldProperties properties, PlayerEntity player) {
        if (cooldown != 0 || player == null) return;
        BlockPos blockPos = player.getBlockPos();

        PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();
        Optional<BlockPos> optional = pointOfInterestStorage.getPosition(registryEntry -> registryEntry.matchesKey(PointOfInterestTypes.MEETING), pos -> true, blockPos, 48, PointOfInterestStorage.OccupationStatus.ANY);
        BlockPos blockPos2 = optional.orElse(blockPos);
        BlockPos blockPos3 = getNearbySpawnPos(world, blockPos2, 48);

        if (blockPos3 == null || !doesNotSuffocateAt(world, blockPos3)) return;
        if (world.getBiome(blockPos3).isIn(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) return;

        WanderingTraderEntity wanderingTraderEntity = EntityType.WANDERING_TRADER.spawn(world, blockPos3, SpawnReason.EVENT);
        if (wanderingTraderEntity == null) return;

        cooldown = 48000;
        for (int j = 0; j < 2; ++j) {
            spawnLlama(world, wanderingTraderEntity);
        }

        properties.setWanderingTraderId(wanderingTraderEntity.getUuid());
        wanderingTraderEntity.setDespawnDelay(48000);
        wanderingTraderEntity.setWanderTarget(blockPos2);
        wanderingTraderEntity.setPositionTarget(blockPos2, 16);
    }

    private void spawnLlama(ServerWorld world, @NotNull WanderingTraderEntity wanderingTrader) {
        MakeSure.notNulls(world, wanderingTrader);
        BlockPos blockPos = this.getNearbySpawnPos(world, wanderingTrader.getBlockPos(), 4);
        if (blockPos == null) return;

        TraderLlamaEntity traderLlamaEntity = EntityType.TRADER_LLAMA.spawn(world, blockPos, SpawnReason.EVENT);
        if (traderLlamaEntity == null) return;

        traderLlamaEntity.attachLeash(wanderingTrader, true);
    }

    @Nullable
    private BlockPos getNearbySpawnPos(WorldView world, BlockPos pos, int range) {
        BlockPos blockPos = null;

        for (int i = 0; i < 10; ++i) {
            int x = pos.getX() + MathStuff.threadRandom().nextInt(range * 2) - range;
            int z = pos.getZ() + MathStuff.threadRandom().nextInt(range * 2) - range;
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos blockPos2 = new BlockPos(x, y, z);
            if (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, world, blockPos2, EntityType.WANDERING_TRADER)) {
                blockPos = blockPos2;
                break;
            }
        }

        return blockPos;
    }

    private boolean doesNotSuffocateAt(BlockView world, BlockPos pos) {
        for (BlockPos blockPos : BlockPos.iterate(pos, pos.add(1, 2, 1))) {
            if (!world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
