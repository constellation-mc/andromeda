package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import net.minecraft.item.Item;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record ItemBehaviorData(List<Item> items, boolean disabled, boolean override_vanilla,
                               boolean complement, int cooldown_time,
                               CommandHolder on_entity_hit, CommandHolder on_block_hit, CommandHolder on_miss, CommandHolder on_any_hit,
                               boolean spawn_item_particles, boolean spawn_colored_particles, ParticleColors particle_colors) {

    public static final Codec<ItemBehaviorData> CODEC = RecordCodecBuilder.create(data -> data.group(
            Codec.either(CommonRegistries.items().getCodec(), Codec.list(CommonRegistries.items().getCodec()))
                    .fieldOf("item_id").xmap(e -> e.map(ImmutableList::of, Function.identity()), Either::right).forGetter(ItemBehaviorData::items),

            Codec.BOOL.optionalFieldOf("disabled", false).forGetter(ItemBehaviorData::disabled),
            Codec.BOOL.optionalFieldOf("override_vanilla", false).forGetter(ItemBehaviorData::override_vanilla),
            Codec.BOOL.optionalFieldOf("complement", true).forGetter(ItemBehaviorData::complement),
            Codec.INT.optionalFieldOf("cooldown_time", 50).forGetter(ItemBehaviorData::cooldown_time),

            CommandHolder.CODEC.optionalFieldOf("on_entity_hit", CommandHolder.EMPTY).forGetter(ItemBehaviorData::on_entity_hit),
            CommandHolder.CODEC.optionalFieldOf("on_block_hit", CommandHolder.EMPTY).forGetter(ItemBehaviorData::on_block_hit),
            CommandHolder.CODEC.optionalFieldOf("on_miss", CommandHolder.EMPTY).forGetter(ItemBehaviorData::on_miss),
            CommandHolder.CODEC.optionalFieldOf("on_any_hit", CommandHolder.EMPTY).forGetter(ItemBehaviorData::on_any_hit),

            Codec.BOOL.optionalFieldOf("spawn_item_particles", true).forGetter(ItemBehaviorData::spawn_item_particles),
            Codec.BOOL.optionalFieldOf("spawn_colored_particles", false).forGetter(ItemBehaviorData::spawn_colored_particles),

            ParticleColors.CODEC.optionalFieldOf("particle_colors", ParticleColors.EMPTY).forGetter(ItemBehaviorData::particle_colors)
    ).apply(data, ItemBehaviorData::new));

    public record ParticleColors(int red, int green, int blue) {
        public static final Codec<ParticleColors> CODEC = RecordCodecBuilder.create(data -> data.group(
                Codec.INT.fieldOf("red").forGetter(ParticleColors::red),
                Codec.INT.fieldOf("green").forGetter(ParticleColors::green),
                Codec.INT.fieldOf("blue").forGetter(ParticleColors::blue)
        ).apply(data, ParticleColors::new));

        public static final ParticleColors EMPTY = new ParticleColors(0,0,0);
    }

    public record CommandHolder(List<String> item_commands, List<String> user_commands, List<String> server_commands,
                                List<String> hit_entity_commands, List<String> hit_block_commands) {
        public static final Codec<CommandHolder> CODEC = RecordCodecBuilder.create(data -> data.group(
                Codec.list(Codec.STRING).optionalFieldOf("item_commands", Collections.emptyList()).forGetter(CommandHolder::item_commands),
                Codec.list(Codec.STRING).optionalFieldOf("user_commands", Collections.emptyList()).forGetter(CommandHolder::user_commands),
                Codec.list(Codec.STRING).optionalFieldOf("server_commands", Collections.emptyList()).forGetter(CommandHolder::server_commands),
                Codec.list(Codec.STRING).optionalFieldOf("hit_entity_commands", Collections.emptyList()).forGetter(CommandHolder::hit_entity_commands),
                Codec.list(Codec.STRING).optionalFieldOf("hit_block_commands", Collections.emptyList()).forGetter(CommandHolder::hit_block_commands)
        ).apply(data, CommandHolder::new));

        public static final CommandHolder EMPTY = CODEC.parse(JsonOps.INSTANCE, new JsonObject()).result().orElseThrow();
    }

    public static ItemBehaviorData create(JsonObject object) {
        return CODEC.parse(JsonOps.INSTANCE, object).getOrThrow(false, string -> {
            throw new RuntimeException(string);
        });
    }
}
