package dev.zenfyr.andromeda.common.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.phys.Vec3;

public class MiscUtil {

  public static final GsonContextImpl lootContext =
      new GsonContextImpl(Deserializers.createLootTableSerializer().create());

  public static double horizontalDistanceTo(Vec3 owner, Vec3 target) {
    double d = target.x - owner.x;
    double f = target.z - owner.z;
    return Math.sqrt(d * d + f * f);
  }

  public static BlockPos vec3dAsBlockPos(Vec3 vec3d) {
    return new BlockPos(Mth.floor(vec3d.x), Mth.floor(vec3d.y), Mth.floor(vec3d.z));
  }

  public static void crudeSetVelocity(Entity entity, double x, double y, double z) {
    crudeSetVelocity(entity, new Vec3(x, y, z));
  }

  public static void crudeSetVelocity(Entity entity, Vec3 velocity) {
    if (!entity.level.isClientSide) {
      entity.setDeltaMovement(velocity);
      for (ServerPlayer player : PlayerLookup.tracking(entity)) {
        player.connection.send(new ClientboundSetEntityMotionPacket(entity));
      }
    } else {
      throw new UnsupportedOperationException(
          "Can't send packets to client unless you're on server.");
    }
  }

  public static final class GsonContextImpl
      implements JsonSerializationContext, JsonDeserializationContext {

    private final Gson gson;

    public GsonContextImpl(Gson gson) {
      this.gson = gson;
    }

    @Override
    public JsonElement serialize(Object src) {
      return gson.toJsonTree(src);
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc) {
      return gson.toJsonTree(src, typeOfSrc);
    }

    @Override
    public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
      return gson.fromJson(json, typeOfT);
    }
  }
}
