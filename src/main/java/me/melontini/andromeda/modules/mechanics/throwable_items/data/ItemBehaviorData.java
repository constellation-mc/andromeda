package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.dark_matter.api.base.util.ColorUtil;
import net.minecraft.item.Item;
import net.minecraft.util.collection.WeightedList;

import java.util.Collections;
import java.util.List;

public record ItemBehaviorData(List<Item> items, boolean disabled, boolean override_vanilla, boolean complement,
                               int cooldown, WeightedList<Commands> commands, Particles particles) {
    public static final Codec<ItemBehaviorData> CODEC = RecordCodecBuilder.create(data -> data.group(
            MiscUtil.listCodec(CommonRegistries.items().getCodec()).fieldOf("items").forGetter(ItemBehaviorData::items),

            Codec.BOOL.optionalFieldOf("disabled", false).forGetter(ItemBehaviorData::disabled),
            Codec.BOOL.optionalFieldOf("override_vanilla", false).forGetter(ItemBehaviorData::override_vanilla),
            Codec.BOOL.optionalFieldOf("complement", true).forGetter(ItemBehaviorData::complement),
            Codec.INT.optionalFieldOf("cooldown", 50).forGetter(ItemBehaviorData::cooldown),

            MiscUtil.weightedListCodec(Commands.CODEC).optionalFieldOf("commands", new WeightedList<>()).forGetter(ItemBehaviorData::commands),
            Particles.CODEC.optionalFieldOf("particles", Particles.EMPTY).forGetter(ItemBehaviorData::particles)
    ).apply(data, ItemBehaviorData::new));

    public record Particles(boolean item, boolean colored, int colors) {
        public static final Codec<Particles> CODEC = RecordCodecBuilder.create(data -> data.group(
                Codec.BOOL.optionalFieldOf("item", true).forGetter(Particles::item),
                Codec.BOOL.optionalFieldOf("colored", true).forGetter(Particles::colored),
                Codec.either(Codec.INT, Codec.intRange(0, 255).listOf()).comapFlatMap(e -> e.map(DataResult::success, integers -> {
                            if (integers.size() != 3)
                                return DataResult.error(() -> "colors array must contain exactly 3 colors (RGB)");
                            return DataResult.success(ColorUtil.toColor(integers.get(0), integers.get(1), integers.get(2)));
                        }), Either::left)
                        .optionalFieldOf("colors", -1).forGetter(Particles::colors)
        ).apply(data, Particles::new));

        public static final Particles EMPTY = CODEC.parse(JsonOps.INSTANCE, new JsonObject()).result().orElseThrow();
    }

    public record Commands(Holder on_entity, Holder on_block, Holder on_miss, Holder on_any) {
        public static final Codec<Commands> CODEC = RecordCodecBuilder.create(data -> data.group(
                Holder.CODEC.optionalFieldOf("on_entity", Holder.EMPTY).forGetter(Commands::on_entity),
                Holder.CODEC.optionalFieldOf("on_block", Holder.EMPTY).forGetter(Commands::on_block),
                Holder.CODEC.optionalFieldOf("on_miss", Holder.EMPTY).forGetter(Commands::on_miss),
                Holder.CODEC.optionalFieldOf("on_any", Holder.EMPTY).forGetter(Commands::on_any)
        ).apply(data, Commands::new));

        public static final Commands EMPTY = CODEC.parse(JsonOps.INSTANCE, new JsonObject()).result().orElseThrow();

        public record Holder(List<String> item, List<String> user, List<String> server, List<String> hit_entity,
                             List<String> hit_block) {
            public static final Codec<Holder> CODEC = RecordCodecBuilder.create(data -> data.group(
                    MiscUtil.listCodec(Codec.STRING).optionalFieldOf("item", Collections.emptyList()).forGetter(Holder::item),
                    MiscUtil.listCodec(Codec.STRING).optionalFieldOf("user", Collections.emptyList()).forGetter(Holder::user),
                    MiscUtil.listCodec(Codec.STRING).optionalFieldOf("server", Collections.emptyList()).forGetter(Holder::server),
                    MiscUtil.listCodec(Codec.STRING).optionalFieldOf("hit_entity", Collections.emptyList()).forGetter(Holder::hit_entity),
                    MiscUtil.listCodec(Codec.STRING).optionalFieldOf("hit_block", Collections.emptyList()).forGetter(Holder::hit_block)
            ).apply(data, Holder::new));

            public static final Holder EMPTY = CODEC.parse(JsonOps.INSTANCE, new JsonObject()).result().orElseThrow();
        }
    }

    public static ItemBehaviorData create(JsonObject object) {
        return CODEC.parse(JsonOps.INSTANCE, object).getOrThrow(false, string -> {
            throw new JsonParseException(string);
        });
    }
}
