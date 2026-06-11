package dev.zenfyr.andromeda.modules.world.crop_temperature;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.IdentifiedJsonDataLoader;
import dev.zenfyr.pulsar.codec.ExtraCodecs;
import dev.zenfyr.pulsar.resources.ReloaderType;
import dev.zenfyr.pulsar.resources.ServerReloadersEvent;
import dev.zenfyr.pulsar.util.MathUtil;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PlantTemperatureData {

  public static final Codec<float[]> FLOAT_ARRAY_CODEC = Codec.FLOAT
      .listOf()
      .comapFlatMap(
          floats -> {
            if (floats.size() != 4)
              return DataResult.error(() -> "temperature array must contain exactly 4 floats!");
            return DataResult.success(new FloatArrayList(floats).toArray(new float[4]));
          },
          floats -> Lists.newArrayList(floats[0], floats[1], floats[2], floats[3]));

  private static final Codec<TemperatureEntry> ENTRY_CODEC =
      RecordCodecBuilder.create(data -> data.group(
              ExtraCodecs.list(BuiltInRegistries.BLOCK.holderByNameCodec())
                  .fieldOf("blocks")
                  .forGetter(TemperatureEntry::blocks),
              FLOAT_ARRAY_CODEC.fieldOf("temperatures").forGetter(TemperatureEntry::temperatures))
          .apply(data, TemperatureEntry::new));

  private static final Codec<NewHolder> HOLDER_CODEC = RecordCodecBuilder.create(
      data -> data.group(Codec.list(ENTRY_CODEC).fieldOf("entries").forGetter(NewHolder::entries))
          .apply(data, NewHolder::new));

  public static final ReloaderType<Reloader> RELOADER =
      ReloaderType.create(Andromeda.id("crop_temperatures"));

  public static boolean roll(BlockState state, float temp, ServerLevel world) {
    float[] data = world.getServer().pulsar$getReloader(RELOADER).get(state.getBlockHolder());
    if (data != null) {
      if (!world.am$get(PlantTemperature.CONFIG).available) return true;

      if ((temp > data[2] && temp <= data[3]) || (temp < data[1] && temp >= data[0])) {
        return MathUtil.nextInt(0, 1) != 0;
      } else return !(temp > data[3]) && !(temp < data[0]);
    }
    return true;
  }

  record TemperatureEntry(List<Holder<Block>> blocks, float[] temperatures) {}

  record NewHolder(List<TemperatureEntry> entries) {}

  public static boolean isPlant(Block block) {
    return block instanceof BushBlock
        || block instanceof GrowingPlantBlock
        || block instanceof BonemealableBlock;
  }

  public static void init() {
    var manager = ModuleManager.get();
    var module = manager.get(PlantTemperature.class).orElseThrow();
    ServerReloadersEvent.EVENT.register(
        context -> context.register(new Reloader(manager, module, context.registryAccess())));
  }

  private static void verifyPostLoad(PlantTemperature module, Reloader reloader) {
    List<Holder.Reference<Block>> override = new ArrayList<>();
    List<Holder.Reference<Block>> blocks = new ArrayList<>();

    BuiltInRegistries.BLOCK.holders().forEach(block -> {
      if (isPlant(block.value()) && reloader.get(block) == null) {
        if (methodInHierarchyUntil(block.value().getClass(), "randomTick", Block.class)) {
          override.add(block);
          return;
        }
        blocks.add(block);
      }
    });

    if (!override.isEmpty())
      module
          .logger()
          .warn(
              "Missing crop temperatures: {}",
              override.stream()
                  .map(Holder.Reference::key)
                  .map(ResourceKey::location)
                  .sorted()
                  .toList());
    if (!blocks.isEmpty())
      module
          .logger()
          .warn(
              "Possible missing crop temperatures: {}",
              blocks.stream()
                  .map(Holder.Reference::key)
                  .map(ResourceKey::location)
                  .sorted()
                  .toList());
  }

  private static boolean methodInHierarchyUntil(Class<?> cls, String name, Class<?> stopClass) {
    if (Arrays.stream(cls.getDeclaredMethods())
        .anyMatch(method -> method.getName().equals(name))) return true;

    return !stopClass.equals(cls.getSuperclass())
        && methodInHierarchyUntil(cls.getSuperclass(), name, stopClass);
  }

  public static class Reloader extends IdentifiedJsonDataLoader {

    @Nullable private HashMap<Holder<Block>, float[]> map;

    private final ModuleManager manager;
    private final PlantTemperature module;
    private final RegistryAccess registryAccess;

    protected Reloader(
        ModuleManager manager, PlantTemperature module, RegistryAccess registryAccess) {
      super(RELOADER.location());
      this.manager = manager;
      this.module = module;
      this.registryAccess = registryAccess;
    }

    public float @Nullable [] get(Holder<Block> block) {
      return Objects.requireNonNull(this.map).get(block);
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      HashMap<Holder<Block>, float[]> result = new HashMap<>();

      RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registryAccess);
      Maps.transformValues(
              data,
              input -> HOLDER_CODEC.parse(ops, input).getOrThrow(false, string -> {
                throw new JsonParseException(string);
              }))
          .values()
          .forEach(newHolder -> newHolder
              .entries()
              .forEach(entry ->
                  entry.blocks().forEach(block -> result.put(block, entry.temperatures()))));
      this.map = result;

      if (this.manager.debug().isVerbose()) verifyPostLoad(module, this);
    }
  }
}
