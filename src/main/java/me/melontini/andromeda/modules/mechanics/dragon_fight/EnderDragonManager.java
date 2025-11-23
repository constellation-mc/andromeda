package me.melontini.andromeda.modules.mechanics.dragon_fight;

import static me.melontini.andromeda.common.Andromeda.id;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.Getter;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;

@SuppressWarnings("UnstableApiUsage")
@Getter
public class EnderDragonManager {

  public static final Codec<EnderDragonManager> CODEC =
      RecordCodecBuilder.create(data -> data.group(
              Codec.INT.fieldOf("maxPlayers").forGetter(EnderDragonManager::getMaxPlayers),
              Crystal.CODEC.listOf().fieldOf("crystals").forGetter(EnderDragonManager::getCrystals))
          .apply(data, EnderDragonManager::new));

  public static final Keeper<AttachmentType<EnderDragonManager>> ATTACHMENT = Keeper.create();

  private final List<Crystal> crystals;
  private int maxPlayers;

  public EnderDragonManager(int maxPlayers, List<Crystal> crystals) {
    this.crystals = new ArrayList<>(crystals);
    this.maxPlayers = Math.max(maxPlayers, 1);
  }

  public void tick(ServerLevel world) {
    List<? extends EnderDragon> dragons = world.getDragons();
    if (dragons.isEmpty()) {
      maxPlayers = 1;
      return;
    }
    int i = Math.max(world.players().size(), 1);
    if (i > maxPlayers) maxPlayers = i;

    Set<Crystal> removal = new HashSet<>();
    for (Crystal pair : crystals) {
      if (pair.timer().decrementAndGet() > 0) continue;

      LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
      lightning.setVisualOnly(true);
      lightning.setPosRaw(pair.pos().x, pair.pos().y, pair.pos().z);
      world.addFreshEntity(lightning);

      ClientboundLevelParticlesPacket particleS2CPacket = new ClientboundLevelParticlesPacket(
          ParticleTypes.END_ROD,
          true,
          pair.pos().x,
          pair.pos().y,
          pair.pos().z,
          0.5f,
          0.5f,
          0.5f,
          0.5f,
          100);
      for (int j = 0; j < world.players().size(); ++j) {
        ServerPlayer serverPlayerEntity = world.players().get(j);
        world.sendParticles(
            serverPlayerEntity, true, pair.pos().x, pair.pos().y, pair.pos().z, particleS2CPacket);
      }

      EndCrystal endCrystalEntity = new EndCrystal(world, pair.pos().x, pair.pos().y, pair.pos().z);
      world.addFreshEntity(endCrystalEntity);
      removal.add(pair);
    }
    crystals.removeAll(removal);

    if (!Andromeda.MAIN.get(DragonFight.CONFIG).scaleHealthByMaxPlayers) return;
    for (EnderDragon dragon : dragons) {
      AttributeInstance inst = dragon.getAttribute(Attributes.MAX_HEALTH);
      MakeSure.notNull(inst, "Ender Dragon has no attributes?")
          .setBaseValue(Math.floor(Math.sqrt(500 * maxPlayers) * 10));
    }
  }

  public void queueRespawn(MutableInt mutableInt, Vec3 vec3d) {
    var crystal = new Crystal(mutableInt, vec3d);
    if (!crystals.contains(crystal)) crystals.add(crystal);
  }

  public record Crystal(MutableInt timer, Vec3 pos) {
    public static final Codec<Crystal> CODEC = RecordCodecBuilder.create(data -> data.group(
            Codec.INT
                .fieldOf("timer")
                .xmap(MutableInt::new, MutableInt::getValue)
                .forGetter(Crystal::timer),
            Vec3.CODEC.fieldOf("pos").forGetter(Crystal::pos))
        .apply(data, Crystal::new));
  }

  static void init() {
    EnderDragonManager.ATTACHMENT.init(AttachmentRegistry.<EnderDragonManager>builder()
        .initializer(() -> new EnderDragonManager(1, Collections.emptyList()))
        .persistent(EnderDragonManager.CODEC)
        .buildAndRegister(id("ender_dragon_data")));

    ServerWorldEvents.LOAD.register((server, world) -> {
      if (world.dimension() == Level.END)
        world.getAttachedOrCreate(EnderDragonManager.ATTACHMENT.get());
    });

    ServerTickEvents.END_WORLD_TICK.register(world -> {
      if (world.dimension() == Level.END)
        world.getAttachedOrCreate(EnderDragonManager.ATTACHMENT.get()).tick(world);
    });
  }
}
