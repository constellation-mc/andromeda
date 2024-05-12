package me.melontini.andromeda.util.commander.bool;

import com.mojang.serialization.Codec;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

public record ConstantBooleanIntermediary(boolean value) implements BooleanIntermediary {

    public static final Codec<ConstantBooleanIntermediary> CODEC = Codec.BOOL.xmap(ConstantBooleanIntermediary::new, ConstantBooleanIntermediary::value);

    @Override
    public boolean asBoolean(Supplier<LootContext> supplier) {
        return this.value;
    }
}
