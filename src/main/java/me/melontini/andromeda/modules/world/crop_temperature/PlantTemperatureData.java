package me.melontini.andromeda.modules.world.crop_temperature;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Mapper;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

import java.lang.invoke.MethodType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static me.melontini.andromeda.util.CommonValues.MODID;

public record PlantTemperatureData(List<Block> blocks, float min, float max, float aMin, float aMax) {

    public static final Codec<PlantTemperatureData> CODEC = RecordCodecBuilder.
            create(data -> data.group(
                    Codec.either(CommonRegistries.blocks().getCodec(), Codec.list(CommonRegistries.blocks().getCodec()))
                            .fieldOf("identifier").xmap(e -> e.map(ImmutableList::of, Function.identity()), Either::right).forGetter(PlantTemperatureData::blocks),
                    Codec.FLOAT.fieldOf("min").forGetter(PlantTemperatureData::min),
                    Codec.FLOAT.fieldOf("max").forGetter(PlantTemperatureData::max),
                    Codec.FLOAT.fieldOf("aMin").forGetter(PlantTemperatureData::aMin),
                    Codec.FLOAT.fieldOf("aMax").forGetter(PlantTemperatureData::aMax)
            ).apply(data, PlantTemperatureData::new));

    public static final Map<Block, PlantTemperatureData> PLANT_DATA = new IdentityHashMap<>();

    public static boolean roll(Block block, float temp, World world) {
        return world.am$get(PlantTemperature.class).enabled && roll(block, temp);
    }

    public static boolean roll(Block block, float temp) {
        if (isPlant(block)) {
            PlantTemperatureData data = PlantTemperatureData.PLANT_DATA.get(block);
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

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PlantTemperatureData.PLANT_DATA.clear());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JsonDataLoader(new Gson(), "andromeda/crop_temperatures") {
            @Override
            public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, PlantTemperatureData> map = new HashMap<>();

                    data.forEach((identifier, object) -> map.put(identifier, CODEC.parse(JsonOps.INSTANCE, object)
                            .getOrThrow(false, string -> {
                                throw new JsonParseException(string);
                            })));

                    return map;
                }, executor).thenAcceptAsync(map -> {
                    map.forEach((identifier, temperatureData) ->
                            temperatureData.blocks.forEach((block) -> PLANT_DATA.put(block, temperatureData)));

                    if (Debug.hasKey(Debug.Keys.PRINT_MISSING_ASSIGNED_DATA))
                        verifyPostLoad();
                }, executor);
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "crop_temperatures");
            }
        });
    }

    private static void verifyPostLoad() {
        String mapped = Mapper.mapMethod(AbstractBlock.class, "method_9514", MethodType.methodType(void.class, BlockState.class, ServerWorld.class, BlockPos.class, Random.class));

        List<Block> override = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();

        CommonRegistries.blocks().forEach(block -> {
            if (isPlant(block) && !PLANT_DATA.containsKey(block)) {
                if (methodInHierarchyUntil(block.getClass(), mapped, PlantBlock.class)) {
                    override.add(block);
                    return;
                }
                blocks.add(block);
            }
        });

        if (!override.isEmpty()) AndromedaLog.warn("Missing crop temperatures: " + override);
        if (!blocks.isEmpty()) AndromedaLog.warn("Possible missing crop temperatures: " + blocks);
    }

    private static boolean methodInHierarchyUntil(Class<?> cls, String name, Class<?> stopClass) {
        if (Arrays.stream(cls.getDeclaredMethods()).anyMatch(method -> method.getName().equals(name)))
            return true;

        return !stopClass.equals(cls.getSuperclass()) && methodInHierarchyUntil(cls.getSuperclass(), name, stopClass);
    }
}
