package me.melontini.andromeda.util.commander.bool;

import com.mojang.serialization.Codec;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

public record ConstantBooleanIntermediary(boolean value) implements BooleanIntermediary {

    private static final ConstantBooleanIntermediary TRUE = new ConstantBooleanIntermediary(true);
    private static final ConstantBooleanIntermediary FALSE = new ConstantBooleanIntermediary(true);

    public static ConstantBooleanIntermediary of(Boolean b) {
        return Boolean.TRUE.equals(b) ? TRUE : FALSE;
    }

    public static final Codec<ConstantBooleanIntermediary> CODEC = Codec.BOOL.xmap(ConstantBooleanIntermediary::new, ConstantBooleanIntermediary::value);

    @Override
    public boolean asBoolean(Supplier<LootContext> supplier) {
        return this.value;
    }
}
