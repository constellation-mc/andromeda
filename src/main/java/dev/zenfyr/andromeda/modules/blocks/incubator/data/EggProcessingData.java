package dev.zenfyr.andromeda.modules.blocks.incubator.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.IdentifiedJsonDataLoader;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.expression.Arithmetica;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

public record EggProcessingData(
    boolean replace, Item item, ShufflingList<Entry> entity, Arithmetica time) {

  public static final Codec<EggProcessingData> CODEC = RecordCodecBuilder.create(data -> data.group(
          ExtraCodecs.optional("replace", Codec.BOOL, false).forGetter(EggProcessingData::replace),
          BuiltInRegistries.ITEM
              .byNameCodec()
              .fieldOf("identifier")
              .forGetter(EggProcessingData::item),
          ExtraCodecs.weightedList(Entry.CODEC)
              .fieldOf("entries")
              .forGetter(EggProcessingData::entity),
          Arithmetica.CODEC.fieldOf("time").forGetter(EggProcessingData::time))
      .apply(data, EggProcessingData::new));

  public record Entry(EntityType<?> type, CompoundTag nbt, List<Command.Conditioned> commands) {
    public static final Codec<Entry> CODEC = RecordCodecBuilder.create(data -> data.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(Entry::type),
            ExtraCodecs.optional("nbt", CompoundTag.CODEC, new CompoundTag()).forGetter(Entry::nbt),
            ExtraCodecs.optional(
                    "commands", ExtraCodecs.list(Command.CODEC.codec()), Collections.emptyList())
                .forGetter(Entry::commands))
        .apply(data, Entry::new));
  }

  public static final ReloaderType<Reloader> RELOADER =
      ReloaderType.create(Andromeda.id("egg_processing"));

  public static void init() {
    ServerReloadersEvent.EVENT.register(context -> context.register(new Reloader()));
  }

  public static class Reloader extends IdentifiedJsonDataLoader {

    private IdentityHashMap<Item, EggProcessingData> map = new IdentityHashMap<>();

    protected Reloader() {
      super(RELOADER.identifier());
    }

    public @Nullable EggProcessingData get(Item item) {
      return this.map.get(item);
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      IdentityHashMap<Item, EggProcessingData> replace = new IdentityHashMap<>();
      IdentityHashMap<Item, EggProcessingData> result = new IdentityHashMap<>();

      for (Item item : BuiltInRegistries.ITEM) {
        if (item instanceof SpawnEggItem egg) {
          ShufflingList<Entry> list = new ShufflingList<>();
          list.add(
              new Entry(egg.getType(new CompoundTag()), new CompoundTag(), Collections.emptyList()),
              1);
          result.put(egg, new EggProcessingData(false, egg, list, Arithmetica.constant(8000)));
        }
      }

      Maps.transformValues(
              data, input -> CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
                throw new RuntimeException(string);
              }))
          .forEach((identifier, eData) -> {
            if (eData.replace()) replace.put(eData.item(), eData);
            else result.put(eData.item(), eData);
          });
      result.putAll(replace);
      this.map = result;
    }
  }
}
