package dev.zenfyr.andromeda.modules.blocks.campfire_effects;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.IdentifiedJsonDataLoader;
import dev.zenfyr.pulsar.codec.ExtraCodecs;
import dev.zenfyr.pulsar.resources.ReloaderType;
import dev.zenfyr.pulsar.resources.ServerReloadersEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class CampfireEffectsData {

  public static final Codec<CampfireEffect> EFFECT_CODEC =
      RecordCodecBuilder.create(data -> data.group(
              BuiltInRegistries.MOB_EFFECT
                  .holderByNameCodec()
                  .fieldOf("effect")
                  .forGetter(CampfireEffect::effect),
              ExtraCodecs.optional("amplifier", Codec.INT, 0).forGetter(CampfireEffect::amplifier))
          .apply(data, CampfireEffect::new));

  public static final Codec<CampfireEffectsEntry> ENTRY_CODEC =
      RecordCodecBuilder.create(data -> data.group(
              ExtraCodecs.list(BuiltInRegistries.BLOCK.holderByNameCodec())
                  .fieldOf("blocks")
                  .forGetter(CampfireEffectsEntry::blocks),
              ExtraCodecs.optional("range", Codec.DOUBLE, 10.0)
                  .forGetter(CampfireEffectsEntry::range),
              ExtraCodecs.optional("affectsPassive", Codec.BOOL, true)
                  .forGetter(CampfireEffectsEntry::affectsPassive),
              Codec.list(EFFECT_CODEC).fieldOf("effects").forGetter(CampfireEffectsEntry::effects))
          .apply(data, CampfireEffectsEntry::new));

  private static final Codec<CampfireEffectsHolder> HOLDER_CODEC =
      RecordCodecBuilder.create(data -> data.group(
              Codec.list(ENTRY_CODEC).fieldOf("entries").forGetter(CampfireEffectsHolder::entries))
          .apply(data, CampfireEffectsHolder::new));

  public static final ReloaderType<Reloader> RELOADER =
      ReloaderType.create(Andromeda.id("campfire_effects"));

  public record CampfireEffect(Holder<MobEffect> effect, int amplifier) {}

  public record CampfireEffectsEntry(
      List<Holder<Block>> blocks,
      double range,
      boolean affectsPassive,
      List<CampfireEffect> effects) {

    public CampfireEffectsEntry noBlocks() {
      return new CampfireEffectsEntry(List.of(), range, affectsPassive, effects);
    }
  }

  public record CampfireEffectsHolder(List<CampfireEffectsEntry> entries) {}

  public static void init() {
    ServerReloadersEvent.EVENT.register(
        context -> context.register(new Reloader(context.registryAccess())));
  }

  public static class Reloader extends IdentifiedJsonDataLoader {

    @Nullable private HashMap<Holder<Block>, CampfireEffectsEntry> map;

    private final RegistryAccess registryAccess;

    protected Reloader(RegistryAccess registryAccess) {
      super(RELOADER.location());
      this.registryAccess = registryAccess;
    }

    public CampfireEffectsEntry get(Holder<Block> block) {
      return Objects.requireNonNull(this.map).get(block);
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      HashMap<Holder<Block>, CampfireEffectsEntry> result = new HashMap<>();

      RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registryAccess);
      Maps.transformValues(
              data,
              input -> HOLDER_CODEC.parse(ops, input).getOrThrow(false, string -> {
                throw new JsonParseException(string);
              }))
          .values()
          .forEach(newHolder -> newHolder
              .entries()
              .forEach(
                  entry -> entry.blocks().forEach(block -> result.put(block, entry.noBlocks()))));
      this.map = result;
    }
  }
}
