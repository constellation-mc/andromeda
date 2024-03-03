package me.melontini.andromeda.modules.mechanics.dragon_fight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

import static me.melontini.andromeda.common.registries.Common.id;

@SuppressWarnings("UnstableApiUsage")
@Getter
public class EnderDragonManager {

    public static final Codec<EnderDragonManager> CODEC = RecordCodecBuilder.create(data -> data.group(
            Codec.INT.fieldOf("maxPlayers").forGetter(EnderDragonManager::getMaxPlayers),
            Crystal.LIST_CODEC.fieldOf("crystals").forGetter(EnderDragonManager::getCrystals)
    ).apply(data, EnderDragonManager::new));

    public static final AttachmentType<EnderDragonManager> ATTACHMENT = AttachmentRegistry.<EnderDragonManager>builder()
            .initializer(() -> new EnderDragonManager(1, Collections.emptyList()))
            .persistent(CODEC)
            .buildAndRegister(id("ender_dragon_data"));

    private final DragonFight module = ModuleManager.quick(DragonFight.class);
    private final List<Crystal> crystals;
    private int maxPlayers;

    public EnderDragonManager(int maxPlayers, List<Crystal> crystals) {
        this.crystals = new ArrayList<>(crystals);
        this.maxPlayers = Math.max(maxPlayers, 1);
    }

    public void tick(ServerWorld world) {
        List<? extends EnderDragonEntity> dragons = world.getAliveEnderDragons();
        if (dragons.isEmpty()) {
            maxPlayers = 1;
            return;
        }
        int i = Math.max(world.getPlayers().size(), 1);
        if (i > maxPlayers) maxPlayers = i;

        Set<Crystal> removal = new HashSet<>();
        for (Crystal pair : crystals) {
            if (pair.timer().decrementAndGet() > 0) continue;

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
            removal.add(pair);
        }
        crystals.removeAll(removal);

        if (!module.config().scaleHealthByMaxPlayers) return;
        for (EnderDragonEntity dragon : dragons) {
            EntityAttributeInstance inst = dragon.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            MakeSure.notNull(inst, "Ender Dragon has no attributes?").setBaseValue(Math.floor((Math.sqrt(500 * maxPlayers)) * 10));
        }
    }

    public void queueRespawn(MutableInt mutableInt, Vec3d vec3d) {
        var crystal = new Crystal(mutableInt, vec3d);
        if (!crystals.contains(crystal)) crystals.add(crystal);
    }

    public record Crystal(MutableInt timer, Vec3d pos) {
        public static final Codec<Crystal> CODEC = RecordCodecBuilder.create(data -> data.group(
                Codec.INT.fieldOf("timer").xmap(MutableInt::new, MutableInt::getValue).forGetter(Crystal::timer),
                Vec3d.CODEC.fieldOf("pos").forGetter(Crystal::pos)
        ).apply(data, Crystal::new));
        public static final Codec<List<Crystal>> LIST_CODEC = CODEC.listOf();
    }
}