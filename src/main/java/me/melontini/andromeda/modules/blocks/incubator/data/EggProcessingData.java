package me.melontini.andromeda.modules.blocks.incubator.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.JsonDataLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public record EggProcessingData(Item item, EntityType<?> entity, int time) {

    public static final Codec<EggProcessingData> CODEC = RecordCodecBuilder.create(data -> data.group(
            CommonRegistries.items().getCodec().fieldOf("identifier").forGetter(EggProcessingData::item),
            CommonRegistries.entityTypes().getCodec().fieldOf("entity").forGetter(EggProcessingData::entity),
            Codec.INT.fieldOf("time").forGetter(EggProcessingData::time)
    ).apply(data, EggProcessingData::new));

    public static Map<Item, EggProcessingData> EGG_DATA = new IdentityHashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> EggProcessingData.EGG_DATA.clear());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JsonDataLoader(Common.id("egg_processing")) {

            @Override
            protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
                Map<Identifier, EggProcessingData> map = new HashMap<>();//TODO dark-matter 4.0.0
                data.forEach((identifier, object) -> map.put(identifier, CODEC.parse(JsonOps.INSTANCE, object)
                        .getOrThrow(false, string -> {
                            throw new RuntimeException(string);
                        })));

                EggProcessingData.EGG_DATA.clear();
                //well...
                for (Item item : CommonRegistries.items()) {
                    if (item instanceof SpawnEggItem spawnEggItem) {
                        EggProcessingData.EGG_DATA.putIfAbsent(spawnEggItem, new EggProcessingData(spawnEggItem, spawnEggItem.getEntityType(new NbtCompound()), 8000));
                    }
                }

                map.forEach((identifier, data1) -> EggProcessingData.EGG_DATA.put(data1.item(), data1));
            }
        });
    }
}
