package me.melontini.andromeda.util.commander;

import com.mojang.serialization.Codec;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;


public record ConstantNumberIntermediary(double value) implements NumberIntermediary {

    public static final Codec<ConstantNumberIntermediary> CODEC = Codec.DOUBLE.xmap(ConstantNumberIntermediary::new, ConstantNumberIntermediary::value);

    @Override
    public double asDouble(Supplier<LootContext> supplier) {
        return this.value;
    }
}
