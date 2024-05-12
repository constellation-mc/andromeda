package me.melontini.andromeda.util.commander.number.constant;

import com.mojang.serialization.Codec;
import me.melontini.andromeda.util.commander.number.LongIntermediary;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

public record ConstantLongIntermediary(long value) implements LongIntermediary {

    public static final Codec<ConstantLongIntermediary> CODEC = Codec.LONG.xmap(ConstantLongIntermediary::new, ConstantLongIntermediary::value);

    @Override
    public long asLong(Supplier<LootContext> supplier) {
        return this.value;
    }
}
