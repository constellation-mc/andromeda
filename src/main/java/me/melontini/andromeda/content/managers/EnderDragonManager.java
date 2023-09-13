package me.melontini.andromeda.content.managers;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.MiscUtil;
import me.melontini.dark_matter.api.minecraft.world.PersistentStateHelper;
import me.melontini.dark_matter.api.minecraft.world.interfaces.DeserializableState;
import me.melontini.dark_matter.api.minecraft.world.interfaces.TickableState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EnderDragonManager extends PersistentState implements DeserializableState, TickableState {

    public static final String ID = "andromeda_ender_dragon_fight";

    private final ServerWorld world;
    private final Set<Pair<MutableInt, Vec3d>> LIST = ConcurrentHashMap.newKeySet();
    private int maxPlayers = 1;

    public EnderDragonManager(ServerWorld world) {
        this.world = world;
    }

    public static EnderDragonManager get(ServerWorld world) {
        return PersistentStateHelper.getOrCreate(world, () -> new EnderDragonManager(world), ID);
    }

    public ServerWorld getWorld() {
        return world;
    }

    public void tick() {
        if (!world.getAliveEnderDragons().isEmpty()) {
            List<? extends EnderDragonEntity> dragons = world.getAliveEnderDragons();

            int i = MathHelper.clamp(world.getPlayers().size(), 1, maxPlayers);
            if (i > maxPlayers) maxPlayers = i;

            for (Pair<MutableInt, Vec3d> pair : LIST) {
                if (pair.getLeft().decrementAndGet() <= 0) {
                    LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                    lightning.setCosmetic(true);
                    lightning.setPos(pair.getRight().x, pair.getRight().y, pair.getRight().z);
                    world.spawnEntity(lightning);

                    ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(ParticleTypes.END_ROD, true, pair.getRight().x, pair.getRight().y, pair.getRight().z, 0.5f, 0.5f, 0.5f, 0.5f, 100);
                    for (int j = 0; j < world.getPlayers().size(); ++j) {
                        ServerPlayerEntity serverPlayerEntity = world.getPlayers().get(j);
                        world.sendToPlayerIfNearby(serverPlayerEntity, true, pair.getRight().x, pair.getRight().y, pair.getRight().z, particleS2CPacket);
                    }

                    EndCrystalEntity endCrystalEntity = new EndCrystalEntity(world, pair.getRight().x, pair.getRight().y, pair.getRight().z);
                    world.spawnEntity(endCrystalEntity);
                    LIST.remove(pair);
                    AndromedaLog.devInfo("respawned crystal at [{}]", MiscUtil.vec3dAsString(pair.getRight()));
                }
            }
            markDirty();
            if (Config.get().dragonFight.scaleHealthByMaxPlayers) {
                for (EnderDragonEntity dragon : dragons) {
                    EntityAttributeInstance inst = dragon.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    inst.setBaseValue(Math.floor((Math.sqrt(500 * i)) * 10));
                }
            }
        } else {
            maxPlayers = 1;
        }
    }

    public void queueRespawn(MutableInt mutableInt, Vec3d vec3d) {
        LIST.add(new Pair<>(mutableInt, vec3d));
        AndromedaLog.devInfo("scheduled crystal at [{}] for respawn in {} ticks", MiscUtil.vec3dAsString(vec3d), mutableInt.getValue());
    }

    public void readNbt(NbtCompound tag) {
        if (tag.contains("players")) {
            NbtList listTag = tag.getList("crystals", 10);
            for (int i = 0; i < listTag.size(); i++) {
                NbtCompound crystal = listTag.getCompound(i);
                MutableInt mutableInt = new MutableInt(crystal.getInt("timer"));
                Vec3d vec3d = new Vec3d(crystal.getDouble("x"), crystal.getDouble("y"), crystal.getDouble("z"));
                LIST.add(new Pair<>(mutableInt, vec3d));
            }
        }
        if (tag.contains("players")) maxPlayers = tag.getInt("players");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList listTag = new NbtList();
        for (Pair<MutableInt, Vec3d> pair : LIST) {
            NbtCompound crystal = new NbtCompound();
            crystal.putInt("timer", pair.getLeft().getValue());
            crystal.putDouble("x", pair.getRight().x);
            crystal.putDouble("y", pair.getRight().y);
            crystal.putDouble("z", pair.getRight().z);
            listTag.add(crystal);
        }
        if (!listTag.isEmpty()) nbt.put("crystals", listTag);
        if (maxPlayers > 1) nbt.putInt("players", maxPlayers);
        return nbt;
    }
}