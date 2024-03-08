package me.melontini.andromeda.modules.world.crop_temperature;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.data.ServerResourceReloadersEvent;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Mapper;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.block.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;

import java.lang.invoke.MethodType;
import java.util.*;

public record PlantTemperatureData(List<Block> blocks, float min, float max, float aMin, float aMax) {

    public static final Codec<PlantTemperatureData> CODEC = RecordCodecBuilder.create(data -> data.group(
            MiscUtil.listCodec(CommonRegistries.blocks().getCodec()).fieldOf("identifier").forGetter(PlantTemperatureData::blocks),
            Codec.FLOAT.fieldOf("min").forGetter(PlantTemperatureData::min),
            Codec.FLOAT.fieldOf("max").forGetter(PlantTemperatureData::max),
            Codec.FLOAT.fieldOf("aMin").forGetter(PlantTemperatureData::aMin),
            Codec.FLOAT.fieldOf("aMax").forGetter(PlantTemperatureData::aMax)
    ).apply(data, PlantTemperatureData::new));

    public static final Identifier RELOADER_ID = Common.id("crop_temperatures");

    public static boolean roll(Block block, float temp, ServerWorld world) {
        if (!world.am$get(PlantTemperature.class).enabled) return false;

        if (isPlant(block)) {
            PlantTemperatureData data = world.getServer().<Reloader>am$getReloader(RELOADER_ID).get(block);
            if (data != null) {
                if ((temp > data.max() && temp <= data.aMax()) || (temp < data.min() && temp >= data.aMin())) {
                    return MathStuff.nextInt(0, 1) != 0;
                } else
                    return (!(temp > data.aMax())) && (!(temp < data.aMin()));
            }
        }
        return true;
    }

    public static boolean isPlant(Block block) {
        return block instanceof PlantBlock || block instanceof AbstractPlantPartBlock;
    }

    public static void init(PlantTemperature module) {
        ServerResourceReloadersEvent.EVENT.register(context -> context.registrar().accept(new Reloader(RELOADER_ID, module)));
    }

    private static void verifyPostLoad(PlantTemperature module, Reloader reloader) {
        String mapped = Mapper.mapMethod(AbstractBlock.class, "method_9514", MethodType.methodType(void.class, BlockState.class, ServerWorld.class, BlockPos.class, Random.class));

        List<Block> override = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();

        CommonRegistries.blocks().forEach(block -> {
            if (isPlant(block) && reloader.get(block) == null) {
                if (methodInHierarchyUntil(block.getClass(), mapped, PlantBlock.class)) {
                    override.add(block);
                    return;
                }
                blocks.add(block);
            }
        });

        if (!override.isEmpty()) module.logger().warn("Missing crop temperatures: " + override);
        if (!blocks.isEmpty()) module.logger().warn("Possible missing crop temperatures: " + blocks);
    }

    private static boolean methodInHierarchyUntil(Class<?> cls, String name, Class<?> stopClass) {
        if (Arrays.stream(cls.getDeclaredMethods()).anyMatch(method -> method.getName().equals(name)))
            return true;

        return !stopClass.equals(cls.getSuperclass()) && methodInHierarchyUntil(cls.getSuperclass(), name, stopClass);
    }

    static class Reloader extends JsonDataLoader {

        private Map<Block, PlantTemperatureData> map;
        private final PlantTemperature module;

        protected Reloader(Identifier id, PlantTemperature module) {
            super(id);
            this.module = module;
        }

        public PlantTemperatureData get(Block block) {
            return this.map.get(block);
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
            Map<Block, PlantTemperatureData> result = new IdentityHashMap<>();
            Maps.transformValues(data, input -> CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
                throw new JsonParseException(string);
            })).forEach((identifier, temperatureData) -> temperatureData.blocks.forEach((block) -> result.put(block, temperatureData)));
            this.map = result;

            if (Debug.Keys.PRINT_MISSING_ASSIGNED_DATA.isPresent()) verifyPostLoad(module, this);
        }
    }
}
