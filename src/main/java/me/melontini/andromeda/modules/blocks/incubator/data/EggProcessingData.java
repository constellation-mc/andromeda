package me.melontini.andromeda.modules.blocks.incubator.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.SneakyThrows;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.andromeda.common.util.MiscUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

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

    public static Map<Item, EggProcessingData> EGG_DATA = new IdentityHashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> EggProcessingData.EGG_DATA.clear());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JsonDataLoader(Common.id("egg_processing")) {

            @SneakyThrows
            @Override
            protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
                Map<Identifier, EggProcessingData> map = new HashMap<>();//TODO dark-matter 4.0.0
                data.forEach((identifier, object) -> map.put(identifier, CODEC.parse(JsonOps.INSTANCE, object)
                        .getOrThrow(false, string -> {
                            throw new RuntimeException(string);
                        })));

                EggProcessingData.EGG_DATA.clear();

                map.forEach((identifier, data1) -> EggProcessingData.EGG_DATA.put(data1.item(), data1));
            }
        });
    }
}
