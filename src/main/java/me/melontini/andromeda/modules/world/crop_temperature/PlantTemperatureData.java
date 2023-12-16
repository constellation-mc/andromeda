package me.melontini.andromeda.modules.world.crop_temperature;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Debug;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.JsonDataLoader;
import me.melontini.dark_matter.api.base.util.Mapper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;

import java.lang.invoke.MethodType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static me.melontini.andromeda.common.registries.ResourceRegistry.parseFromId;
import static me.melontini.andromeda.util.CommonValues.MODID;

public record PlantTemperatureData(Block block, float min, float max, float aMin, float aMax) {

    public static final Map<Block, PlantTemperatureData> PLANT_DATA = new HashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PlantTemperatureData.PLANT_DATA.clear());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JsonDataLoader(new Gson(), "andromeda/crop_temperatures") {
            @Override
            public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, PlantTemperatureData> map = new HashMap<>();

                    data.forEach((identifier, object) -> map.put(identifier, new PlantTemperatureData(parseFromId(object.get("identifier").getAsString(), CommonRegistries.blocks()),
                            object.get("min").getAsFloat(),
                            object.get("max").getAsFloat(),
                            object.get("aMin").getAsFloat(),
                            object.get("aMax").getAsFloat())));

                    return map;
                }, executor).thenAcceptAsync(map -> {
                    map.forEach((identifier, temperatureData) ->
                            PLANT_DATA.put(temperatureData.block, temperatureData));

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

        CommonRegistries.blocks().forEach(block1 -> {
            if (block1 instanceof PlantBlock && !PLANT_DATA.containsKey(block1)) {
                if (methodInHierarchyUntil(block1.getClass(), mapped, PlantBlock.class)) {
                    override.add(block1);
                    return;
                }
                blocks.add(block1);
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
