package me.melontini.andromeda.modules.mechanics.dragon_fight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.dark_matter.api.base.util.MakeSure;
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
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EnderDragonManager extends PersistentState implements DeserializableState, TickableState {

    public static final String ID = "andromeda_ender_dragon_fight";

    private final DragonFight module = ModuleManager.quick(DragonFight.class);
    private final List<Crystal> crystals = new ArrayList<>();
    private final ServerWorld world;
    private int maxPlayers = 1;

    public EnderDragonManager(ServerWorld world) {
        this.world = world;
    }

    public static EnderDragonManager get(ServerWorld world) {
        return PersistentStateHelper.getOrCreate(world, () -> new EnderDragonManager(world), ID);
    }

    public void tick() {
        if (!world.getAliveEnderDragons().isEmpty()) {
            List<? extends EnderDragonEntity> dragons = world.getAliveEnderDragons();

            int i = MathHelper.clamp(world.getPlayers().size(), 1, maxPlayers);
            if (i > maxPlayers) maxPlayers = i;

            for (Crystal pair : crystals) {
                if (pair.timer().decrementAndGet() <= 0) {
                    LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                    lightning.setCosmetic(true);
                    lightning.setPos(pair.pos().x, pair.pos().y, pair.pos().z);
                    world.spawnEntity(lightning);

                    ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(ParticleTypes.END_ROD, true, pair.pos().x, pair.pos().y, pair.pos().z, 0.5f, 0.5f, 0.5f, 0.5f, 100);
                    for (int j = 0; j < world.getPlayers().size(); ++j) {
                        ServerPlayerEntity serverPlayerEntity = world.getPlayers().get(j);
                        world.sendToPlayerIfNearby(serverPlayerEntity, true, pair.pos().x, pair.pos().y, pair.pos().z, particleS2CPacket);
                    }

                    EndCrystalEntity endCrystalEntity = new EndCrystalEntity(world, pair.pos().x, pair.pos().y, pair.pos().z);
                    world.spawnEntity(endCrystalEntity);
                    crystals.remove(pair);
                }
            }
            markDirty();
            if (module.config().scaleHealthByMaxPlayers) {
                for (EnderDragonEntity dragon : dragons) {
                    EntityAttributeInstance inst = dragon.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    MakeSure.notNull(inst, "Ender Dragon has no attributes?").setBaseValue(Math.floor((Math.sqrt(500 * i)) * 10));
                }
            }
        } else {
            maxPlayers = 1;
        }
    }

    public void queueRespawn(MutableInt mutableInt, Vec3d vec3d) {
        var crystal = new Crystal(mutableInt, vec3d);
        if (!crystals.contains(crystal)) crystals.add(crystal);
    }

    public void readNbt(NbtCompound tag) {
        if (tag.contains("crystals")) {
            NbtList listTag = tag.getList("crystals", 10);
            for (int i = 0; i < listTag.size(); i++) {
                NbtCompound crystal = listTag.getCompound(i);
                MutableInt mutableInt = new MutableInt(crystal.getInt("timer"));
                Vec3d vec3d = new Vec3d(crystal.getDouble("x"), crystal.getDouble("y"), crystal.getDouble("z"));
                crystals.add(new Crystal(mutableInt, vec3d));
            }
        }

        if (tag.contains("crystalData"))
            crystals.addAll(Crystal.LIST_CODEC.parse(NbtOps.INSTANCE, tag.getCompound("crystalData"))
                    .getOrThrow(false, string -> {
                        throw new IllegalStateException(string);
                    }));
        if (tag.contains("players")) maxPlayers = tag.getInt("players");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        if (!crystals.isEmpty()) nbt.put("crystalData", Crystal.LIST_CODEC.encodeStart(NbtOps.INSTANCE, crystals)
                .getOrThrow(false, string -> {
                    throw new IllegalStateException(string);
                }));
        if (maxPlayers > 1) nbt.putInt("players", maxPlayers);
        return nbt;
    }

    public record Crystal(MutableInt timer, Vec3d pos) {
        public static final Codec<Crystal> CODEC = RecordCodecBuilder.create(data -> data.group(
                Codec.INT.fieldOf("timer").xmap(MutableInt::new, MutableInt::getValue).forGetter(Crystal::timer),
                Vec3d.CODEC.fieldOf("pos").forGetter(Crystal::pos)
        ).apply(data, Crystal::new));
        public static final Codec<List<Crystal>> LIST_CODEC = CODEC.listOf();
    }
}