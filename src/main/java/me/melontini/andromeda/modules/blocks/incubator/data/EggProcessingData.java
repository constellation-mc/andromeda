package me.melontini.andromeda.modules.blocks.incubator.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.data.ServerResourceReloadersEvent;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.profiler.Profiler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public record EggProcessingData(Item item, WeightedList<Entry> entity, int time) {

    public static final Codec<EggProcessingData> CODEC = RecordCodecBuilder.create(data -> data.group(
            CommonRegistries.items().getCodec().fieldOf("identifier").forGetter(EggProcessingData::item),
            MiscUtil.weightedListCodec(Entry.CODEC).fieldOf("entries").forGetter(EggProcessingData::entity),
            Codec.INT.fieldOf("time").forGetter(EggProcessingData::time)
    ).apply(data, EggProcessingData::new));

    public record Entry(EntityType<?> type, NbtCompound nbt, List<String> commands) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(data -> data.group(
                CommonRegistries.entityTypes().getCodec().fieldOf("entity").forGetter(Entry::type),
                NbtCompound.CODEC.optionalFieldOf("nbt", new NbtCompound()).forGetter(Entry::nbt),
                MiscUtil.listCodec(Codec.STRING).optionalFieldOf("commands", Collections.emptyList()).forGetter(Entry::commands)
        ).apply(data, Entry::new));
    }

    public static final Identifier RELOADER_ID = Common.id("egg_processing");

    public static void init() {
        ServerResourceReloadersEvent.EVENT.register(context -> context.register(new Reloader(RELOADER_ID)));
    }

    public static EggProcessingData get(MinecraftServer server, Item item) {
        return MakeSure.notNull(server).<EggProcessingData.Reloader>am$getReloader(EggProcessingData.RELOADER_ID).get(item);
    }

    public static class Reloader extends JsonDataLoader {

        private Map<Item, EggProcessingData> map;

        protected Reloader(Identifier id) {
            super(id);
        }

        public EggProcessingData get(Item item) {
            return this.map.get(item);
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
            Map<Item, EggProcessingData> result = new IdentityHashMap<>();
            Maps.transformValues(data, input -> CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
                throw new RuntimeException(string);
            })).forEach((identifier, eData) -> result.put(eData.item(), eData));
            this.map = result;
        }
    }
}
