package me.melontini.andromeda.util.commander.number.constant;

import com.mojang.serialization.Codec;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;


public record ConstantDoubleIntermediary(double value) implements DoubleIntermediary {

    public static final Codec<ConstantDoubleIntermediary> CODEC = Codec.DOUBLE.xmap(ConstantDoubleIntermediary::new, ConstantDoubleIntermediary::value);

    @Override
    public double asDouble(Supplier<LootContext> supplier) {
        return this.value;
    }
}
