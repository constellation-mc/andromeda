package me.melontini.andromeda.common.util.commander.number.constant;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import me.melontini.andromeda.common.util.commander.number.DoubleIntermediary;
import net.minecraft.loot.context.LootContext;

public record ConstantDoubleIntermediary(double value) implements DoubleIntermediary {

  public static final Codec<ConstantDoubleIntermediary> CODEC =
      Codec.DOUBLE.xmap(ConstantDoubleIntermediary::new, ConstantDoubleIntermediary::value);

  @Override
  public double asDouble(Supplier<LootContext> supplier) {
    return this.value;
  }
}
