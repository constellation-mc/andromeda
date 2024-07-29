package me.melontini.andromeda.modules.world.crop_temperature;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.Function;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.IdentifiedJsonDataLoader;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Mapper;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

public final class PlantTemperatureData {

  private static final Codec<OldHolder> OLD_CODEC = RecordCodecBuilder.create(data -> data.group(
          ExtraCodecs.optional("replace", Codec.BOOL, false).forGetter(OldHolder::replace),
          ExtraCodecs.list(Registries.BLOCK.getCodec())
              .fieldOf("identifier")
              .forGetter(OldHolder::blocks),
          Codec.FLOAT.fieldOf("min").forGetter(o -> o.temperatures()[1]),
          Codec.FLOAT.fieldOf("max").forGetter(o -> o.temperatures()[2]),
          Codec.FLOAT.fieldOf("aMin").forGetter(o -> o.temperatures()[0]),
          Codec.FLOAT.fieldOf("aMax").forGetter(o -> o.temperatures()[3]))
      .apply(
          data,
          (o, blocks, f1, f2, f3, f4) -> new OldHolder(o, blocks, new float[] {f1, f2, f3, f4})));

  public static final Codec<float[]> FLOAT_ARRAY_CODEC = Codec.FLOAT
      .listOf()
      .comapFlatMap(
          floats -> {
            if (floats.size() != 4)
              return DataResult.error(() -> "temperature array must contain exactly 4 floats!");
            return DataResult.success(new FloatArrayList(floats).toArray(new float[4]));
          },
          floats -> Lists.newArrayList(floats[0], floats[1], floats[2], floats[3]));

  private static final Codec<NewHolder> BASE_HOLDER = RecordCodecBuilder.create(data -> data.group(
          ExtraCodecs.optional("replace", Codec.BOOL, false).forGetter(NewHolder::replace),
          Codec.unboundedMap(Registries.BLOCK.getCodec(), FLOAT_ARRAY_CODEC)
              .fieldOf("entries")
              .forGetter(NewHolder::temperatures))
      .apply(data, NewHolder::new));

  private static final Codec<NewHolder> MERGED_CODEC = ExtraCodecs.either(
          OLD_CODEC.xmap(
              pair -> {
                Map<Block, float[]> map = new LinkedHashMap<>();
                pair.blocks().forEach(block1 -> map.put(block1, pair.temperatures));
                return new NewHolder(pair.replace(), map);
              },
              newHolder -> new OldHolder(
                  newHolder.replace(),
                  List.copyOf(newHolder.temperatures.keySet()),
                  newHolder.temperatures.values().stream()
                      .findFirst()
                      .orElseGet(() -> new float[4]))),
          BASE_HOLDER)
      .xmap(e -> e.map(Function.identity(), Function.identity()), Either::left);

  public static final ReloaderType<Reloader> RELOADER =
      ReloaderType.create(Andromeda.id("crop_temperatures"));

  public static boolean roll(BlockPos pos, BlockState state, float temp, ServerWorld world) {
    if (isPlant(state.getBlock())) {
      float[] data = world.getServer().dm$getReloader(RELOADER).get(state.getBlock());
      if (data != null) {
        if (!world
            .am$get(PlantTemperature.CONFIG)
            .available
            .asBoolean(LootContextUtil.block(world, Vec3d.ofCenter(pos), state))) return true;

        if ((temp > data[2] && temp <= data[3]) || (temp < data[1] && temp >= data[0])) {
          return MathUtil.nextInt(0, 1) != 0;
        } else return !(temp > data[3]) && !(temp < data[0]);
      }
    }
    return true;
  }

  record OldHolder(boolean replace, List<Block> blocks, float[] temperatures) {}

  record NewHolder(boolean replace, Map<Block, float[]> temperatures) {}

  public static boolean isPlant(Block block) {
    return block instanceof PlantBlock
        || block instanceof AbstractPlantPartBlock
        || block instanceof Fertilizable;
  }

  public static void init(PlantTemperature module) {
    ServerReloadersEvent.EVENT.register(context -> context.register(new Reloader(module)));
  }

  private static void verifyPostLoad(PlantTemperature module, Reloader reloader) {
    String mapped = Mapper.mapMethod(
        AbstractBlock.class,
        "method_9514",
        MethodType.methodType(
            void.class, BlockState.class, ServerWorld.class, BlockPos.class, Random.class));

    List<Block> override = new ArrayList<>();
    List<Block> blocks = new ArrayList<>();

    Registries.BLOCK.forEach(block -> {
      if (isPlant(block) && reloader.get(block) == null) {
        if (methodInHierarchyUntil(block.getClass(), mapped, Block.class)) {
          override.add(block);
          return;
        }
        blocks.add(block);
      }
    });

    if (!override.isEmpty())
      module
          .logger()
          .warn("Missing crop temperatures: "
              + override.stream().map(Registries.BLOCK::getId).sorted().toList());
    if (!blocks.isEmpty())
      module
          .logger()
          .warn("Possible missing crop temperatures: "
              + blocks.stream().map(Registries.BLOCK::getId).sorted().toList());
  }

  private static boolean methodInHierarchyUntil(Class<?> cls, String name, Class<?> stopClass) {
    if (Arrays.stream(cls.getDeclaredMethods())
        .anyMatch(method -> method.getName().equals(name))) return true;

    return !stopClass.equals(cls.getSuperclass())
        && methodInHierarchyUntil(cls.getSuperclass(), name, stopClass);
  }

  public static class Reloader extends IdentifiedJsonDataLoader {

    @Nullable private IdentityHashMap<Block, float[]> map;

    private final PlantTemperature module;

    protected Reloader(PlantTemperature module) {
      super(RELOADER.identifier());
      this.module = module;
    }

    public float @Nullable [] get(Block block) {
      return Objects.requireNonNull(this.map).get(block);
    }

    @Override
    protected void apply(
        Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
      IdentityHashMap<Block, float[]> replace = new IdentityHashMap<>();
      IdentityHashMap<Block, float[]> result = new IdentityHashMap<>();
      Maps.transformValues(
              data,
              input -> MERGED_CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
                throw new JsonParseException(string);
              }))
          .values()
          .forEach(newHolder -> {
            if (newHolder.replace()) replace.putAll(newHolder.temperatures());
            else result.putAll(newHolder.temperatures());
          });
      result.putAll(replace);
      this.map = result;

      if (Debug.Keys.PRINT_MISSING_ASSIGNED_DATA.isPresent()) verifyPostLoad(module, this);
    }
  }
}
