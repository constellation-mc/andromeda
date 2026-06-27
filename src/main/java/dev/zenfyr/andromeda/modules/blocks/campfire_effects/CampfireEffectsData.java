package dev.zenfyr.andromeda.modules.blocks.campfire_effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.pulsar.api.codec.ExtraCodecs;
import dev.zenfyr.pulsar.api.codec.JsonCodecDataLoader;
import dev.zenfyr.pulsar.api.loot.LootCodecs;
import dev.zenfyr.pulsar.api.resources.ReloaderType;
import dev.zenfyr.pulsar.api.resources.ServerReloadersEvent;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

public class CampfireEffectsData {

  public static final Codec<CampfireEffect> EFFECT_CODEC =
      RecordCodecBuilder.create(data -> data.group(
              BuiltInRegistries.MOB_EFFECT
                  .holderByNameCodec()
                  .fieldOf("effect")
                  .forGetter(CampfireEffect::effect),
              ExtraCodecs.optional("amplifier", Codec.INT, 0).forGetter(CampfireEffect::amplifier),
              LootCodecs.ITEM_CONDITION_CODEC
                  .optionalFieldOf("condition")
                  .forGetter(CampfireEffect::condition))
          .apply(data, CampfireEffect::new));

  public static final Codec<CampfireEffectsEntry> ENTRY_CODEC =
      RecordCodecBuilder.create(data -> data.group(
              ExtraCodecs.optional("range", Codec.DOUBLE, 10.0)
                  .forGetter(CampfireEffectsEntry::range),
              ExtraCodecs.optional("affectsPassive", Codec.BOOL, true)
                  .forGetter(CampfireEffectsEntry::affectsPassive),
              LootCodecs.ITEM_CONDITION_CODEC
                  .optionalFieldOf("condition")
                  .forGetter(CampfireEffectsEntry::condition),
              Codec.list(EFFECT_CODEC).fieldOf("effects").forGetter(CampfireEffectsEntry::effects))
          .apply(data, CampfireEffectsEntry::new));

  private static final Codec<Map<Holder<Block>, CampfireEffectsEntry>> BASE_HOLDER =
      Codec.unboundedMap(BuiltInRegistries.BLOCK.holderByNameCodec(), ENTRY_CODEC);

  public static final ReloaderType<Reloader> RELOADER =
      ReloaderType.create(Andromeda.id("campfire_effects"));

  public record CampfireEffect(
      Holder<MobEffect> effect, int amplifier, Optional<LootItemCondition> condition) {}

  public record CampfireEffectsEntry(
      double range,
      boolean affectsPassive,
      Optional<LootItemCondition> condition,
      List<CampfireEffect> effects) {}

  public static void init() {
    ServerReloadersEvent.EVENT.register(context -> context.register(new Reloader()));
  }

  public static class Reloader
      extends JsonCodecDataLoader<Map<Holder<Block>, CampfireEffectsEntry>> {

    @Nullable private Map<Holder<Block>, CampfireEffectsEntry> map;

    protected Reloader() {
      super(RELOADER.location(), BASE_HOLDER);
    }

    public CampfireEffectsEntry get(Holder<Block> block) {
      return Objects.requireNonNull(this.map).get(block);
    }

    @Override
    protected void apply(
        Map<ResourceLocation, Map<Holder<Block>, CampfireEffectsEntry>> data,
        ResourceManager manager) {

      Map<Holder<Block>, CampfireEffectsEntry> result = new HashMap<>();
      for (Map.Entry<ResourceLocation, Map<Holder<Block>, CampfireEffectsEntry>> entry :
          data.entrySet()) {
        result.putAll(entry.getValue());
      }
      this.map = result;
    }
  }
}
