package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.Event;
import me.melontini.dark_matter.api.minecraft.data.ExtraCodecs;
import net.minecraft.item.Item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ItemBehaviorData(List<Item> items, boolean disabled, boolean override_vanilla, boolean complement,
                               Optional<Integer> cooldown, List<Event> events) {

    public static final Codec<ItemBehaviorData> CODEC = RecordCodecBuilder.create(data -> data.group(
            ExtraCodecs.list(CommonRegistries.items().getCodec()).fieldOf("items").forGetter(ItemBehaviorData::items),

            Codec.BOOL.optionalFieldOf("disabled", false).forGetter(ItemBehaviorData::disabled),
            Codec.BOOL.optionalFieldOf("override_vanilla", false).forGetter(ItemBehaviorData::override_vanilla),
            Codec.BOOL.optionalFieldOf("complement", true).forGetter(ItemBehaviorData::complement),
            Codec.INT.optionalFieldOf("cooldown").forGetter(ItemBehaviorData::cooldown),

            ExtraCodecs.list(Event.CODEC).optionalFieldOf("events", Collections.emptyList()).forGetter(ItemBehaviorData::events)
    ).apply(data, ItemBehaviorData::new));
}
