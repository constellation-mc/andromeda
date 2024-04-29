package me.melontini.andromeda.modules.world.crop_temperature;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.util.IdentifiedJsonDataLoader;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Mapper;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import net.minecraft.block.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodType;
import java.util.*;

public record PlantTemperatureData(List<Block> blocks, float min, float max, float aMin, float aMax) {

    public static final Codec<PlantTemperatureData> CODEC = RecordCodecBuilder.create(data -> data.group(
            ExtraCodecs.list(CommonRegistries.blocks().getCodec()).fieldOf("identifier").forGetter(PlantTemperatureData::blocks),
            Codec.FLOAT.fieldOf("min").forGetter(PlantTemperatureData::min),
            Codec.FLOAT.fieldOf("max").forGetter(PlantTemperatureData::max),
            Codec.FLOAT.fieldOf("aMin").forGetter(PlantTemperatureData::aMin),
            Codec.FLOAT.fieldOf("aMax").forGetter(PlantTemperatureData::aMax)
    ).apply(data, PlantTemperatureData::new));

    public static final ReloaderType<Reloader> RELOADER = ReloaderType.create(Andromeda.id("crop_temperatures"));

    public static boolean roll(Block block, float temp, ServerWorld world) {
        if (!world.am$get(PlantTemperature.class).enabled) return false;

        if (isPlant(block)) {
            PlantTemperatureData data = world.getServer().dm$getReloader(RELOADER).get(block);
            if (data != null) {
                if ((temp > data.max() && temp <= data.aMax()) || (temp < data.min() && temp >= data.aMin())) {
                    return MathUtil.nextInt(0, 1) != 0;
                } else
                    return !(temp > data.aMax()) && !(temp < data.aMin());
            }
        }
        return true;
    }

    public static boolean isPlant(Block block) {
        return block instanceof PlantBlock || block instanceof AbstractPlantPartBlock;
    }

    public static void init(PlantTemperature module) {
        ServerReloadersEvent.EVENT.register(context -> context.register(new Reloader(module)));
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

        if (!override.isEmpty()) module.logger().warn("Missing crop temperatures: " + override.stream().map(block -> CommonRegistries.blocks().getId(block)).toList());
        if (!blocks.isEmpty()) module.logger().warn("Possible missing crop temperatures: " + blocks.stream().map(block -> CommonRegistries.blocks().getId(block)).toList());
    }

    private static boolean methodInHierarchyUntil(Class<?> cls, String name, Class<?> stopClass) {
        if (Arrays.stream(cls.getDeclaredMethods()).anyMatch(method -> method.getName().equals(name)))
            return true;

        return !stopClass.equals(cls.getSuperclass()) && methodInHierarchyUntil(cls.getSuperclass(), name, stopClass);
    }

    public static class Reloader extends IdentifiedJsonDataLoader {

        @Nullable private IdentityHashMap<Block, PlantTemperatureData> map;
        private final PlantTemperature module;

        protected Reloader(PlantTemperature module) {
            super(RELOADER.identifier());
            this.module = module;
        }

        public @Nullable PlantTemperatureData get(Block block) {
            return Objects.requireNonNull(this.map).get(block);
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
            IdentityHashMap<Block, PlantTemperatureData> result = new IdentityHashMap<>();
            Maps.transformValues(data, input -> CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
                throw new JsonParseException(string);
            })).forEach((identifier, temperatureData) -> temperatureData.blocks.forEach((block) -> result.put(block, temperatureData)));
            this.map = result;

            if (Debug.Keys.PRINT_MISSING_ASSIGNED_DATA.isPresent()) verifyPostLoad(module, this);
        }
    }
}
