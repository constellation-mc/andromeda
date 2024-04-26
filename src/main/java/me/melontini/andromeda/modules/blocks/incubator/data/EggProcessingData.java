package me.melontini.andromeda.modules.blocks.incubator.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.IdentifiedJsonDataLoader;
import me.melontini.commander.api.command.Command;
import me.melontini.commander.api.expression.Arithmetica;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public record EggProcessingData(Item item, WeightedList<Entry> entity, Arithmetica time) {

    public static final Codec<EggProcessingData> CODEC = RecordCodecBuilder.create(data -> data.group(
            CommonRegistries.items().getCodec().fieldOf("identifier").forGetter(EggProcessingData::item),
            ExtraCodecs.weightedList(Entry.CODEC).fieldOf("entries").forGetter(EggProcessingData::entity),
            Arithmetica.CODEC.fieldOf("time").forGetter(EggProcessingData::time)
    ).apply(data, EggProcessingData::new));

    public record Entry(EntityType<?> type, NbtCompound nbt, List<Command.Conditioned> commands) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(data -> data.group(
                CommonRegistries.entityTypes().getCodec().fieldOf("entity").forGetter(Entry::type),
                NbtCompound.CODEC.optionalFieldOf("nbt", new NbtCompound()).forGetter(Entry::nbt),
                ExtraCodecs.list(Command.CODEC).optionalFieldOf("commands", Collections.emptyList()).forGetter(Entry::commands)
        ).apply(data, Entry::new));
    }

    public static final ReloaderType<Reloader> RELOADER = ReloaderType.create(Common.id("egg_processing"));

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
        protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
            IdentityHashMap<Item, EggProcessingData> result = new IdentityHashMap<>();

            for (Item item : CommonRegistries.items()) {
                if (item instanceof SpawnEggItem egg) {
                    WeightedList<Entry> list =  new WeightedList<>();
                    list.add(new Entry(egg.getEntityType(new NbtCompound()), new NbtCompound(), Collections.emptyList()), 1);
                    result.put(egg, new EggProcessingData(egg, list, Arithmetica.constant(8000)));
                }
            }

            Maps.transformValues(data, input -> CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
                throw new RuntimeException(string);
            })).forEach((identifier, eData) -> result.put(eData.item(), eData));
            this.map = result;
        }
    }
}
