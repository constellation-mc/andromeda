package dev.zenfyr.andromeda.modules.world.crop_temperature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.pulsar.api.codec.JsonCodecDataLoader;
import dev.zenfyr.pulsar.api.resources.ReloadListenerType;
import dev.zenfyr.pulsar.api.resources.ServerReloadListenersEvent;
import dev.zenfyr.pulsar.api.util.MathUtil;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
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
            var array = new FloatArrayList(floats).toArray(new float[4]);
            if (!isValidEntry(array)) {
              return DataResult.error(
                  () ->
                      "temperature array must not contain NaN and values must be amin <= min <= max <= amax");
            }
            return DataResult.success(array);
          },
          floats -> Lists.newArrayList(floats[0], floats[1], floats[2], floats[3]));

  private static final Codec<TemperatureEntry> ENTRY_CODEC =
      RecordCodecBuilder.create(data -> data.group(
              FLOAT_ARRAY_CODEC.fieldOf("temperatures").forGetter(TemperatureEntry::temperatures))
          .apply(data, TemperatureEntry::new));

  private static final Codec<Map<Holder<Block>, TemperatureEntry>> BASE_HOLDER =
      Codec.unboundedMap(BuiltInRegistries.BLOCK.holderByNameCodec(), ENTRY_CODEC);

  public static final ReloadListenerType<Reloader> RELOADER =
      ReloadListenerType.create(Andromeda.id("crop_temperatures"));

  public static boolean roll(BlockPos pos, BlockState state, float temp, ServerLevel world) {
    var entry = world.getServer().pulsar$getReloadListener(RELOADER).get(state.getBlockHolder());
    if (entry != null) {
      if (!world.am$get(PlantTemperature.CONFIG).available) return true;
      var data = entry.temperatures();

      if ((temp > data[2] && temp <= data[3]) || (temp < data[1] && temp >= data[0])) {
        return MathUtil.nextInt(0, 1) != 0;
      } else return !(temp > data[3]) && !(temp < data[0]);
    }
    return true;
  }

  private static boolean isValidEntry(float[] data) {
    if (data == null || data.length != 4) return false;
    for (float f : data) if (Float.isNaN(f)) return false;
    return data[0] <= data[1] && data[1] <= data[2] && data[2] <= data[3];
  }

  public record TemperatureEntry(float[] temperatures) {}

  public static boolean isPlant(Block block) {
    return block instanceof BushBlock
        || block instanceof GrowingPlantBlock
        || block instanceof BonemealableBlock
        || block instanceof CactusBlock;
  }

  public static void init() {
    var manager = ModuleManager.get();
    var module = manager.get(PlantTemperature.class).orElseThrow();
    ServerReloadListenersEvent.EVENT.listen(
        context -> context.register(RELOADER.location(), new Reloader(manager, module)));
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

  public static class Reloader extends JsonCodecDataLoader<Map<Holder<Block>, TemperatureEntry>> {

    @Nullable private Map<Holder<Block>, TemperatureEntry> map;

    private final ModuleManager manager;
    private final PlantTemperature module;

    protected Reloader(ModuleManager manager, PlantTemperature module) {
      super(RELOADER.location(), BASE_HOLDER);
      this.manager = manager;
      this.module = module;
    }

    public TemperatureEntry get(Holder<Block> block) {
      return Objects.requireNonNull(this.map).get(block);
    }

    @Override
    protected void apply(
        Map<ResourceLocation, Map<Holder<Block>, TemperatureEntry>> data, ResourceManager manager) {

      Map<Holder<Block>, TemperatureEntry> result = new HashMap<>();
      for (Map.Entry<ResourceLocation, Map<Holder<Block>, TemperatureEntry>> entry :
          data.entrySet()) {
        result.putAll(entry.getValue());
      }
      this.map = result;

      if (this.manager.debug().isVerbose()) verifyPostLoad(module, this);
    }
  }
}
