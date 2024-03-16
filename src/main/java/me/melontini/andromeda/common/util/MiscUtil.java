package me.melontini.andromeda.common.util;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Type;

public class MiscUtil {

    public static final GsonContextImpl lootContext = new GsonContextImpl(LootGsons.getConditionGsonBuilder().create());

    public static final Codec<LootCondition> LOOT_CONDITION_CODEC = Codecs.JSON_ELEMENT.flatXmap(element -> {
        if (!element.isJsonObject()) return DataResult.error(() -> "'%s' not a JsonObject".formatted(element));
        JsonObject object = element.getAsJsonObject();

        LootConditionType type = Registries.LOOT_CONDITION_TYPE.get(Identifier.tryParse(object.get("condition").getAsString()));
        if (type == null)
            return DataResult.error(() -> "No such condition type '%s'".formatted(object.get("condition").getAsString()));
        return DataResult.success((LootCondition) type.getJsonSerializer().fromJson(object, lootContext));
    }, condition -> {
        JsonObject object = new JsonObject();
        condition.getType().getJsonSerializer().toJson(object, Utilities.cast(condition), lootContext);
        return DataResult.success(object);
    });

    public static double horizontalDistanceTo(Vec3d owner, Vec3d target) {
        double d = target.x - owner.x;
        double f = target.z - owner.z;
        return Math.sqrt(d * d + f * f);
    }

    public static String blockPosAsString(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static String vec3dAsString(Vec3d vec3d) {
        return vec3d.getX() + ", " + vec3d.getY() + ", " + vec3d.getZ();
    }

    public static BlockPos vec3dAsBlockPos(Vec3d vec3d) {
        return new BlockPos(MathHelper.floor(vec3d.x), MathHelper.floor(vec3d.y), MathHelper.floor(vec3d.z));
    }

    public static RegistryEntry.Reference<DamageType> getTypeReference(World world, RegistryKey<DamageType> type) {
        return world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(type).orElseThrow();
    }

    public static final class GsonContextImpl implements JsonSerializationContext, JsonDeserializationContext {

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
