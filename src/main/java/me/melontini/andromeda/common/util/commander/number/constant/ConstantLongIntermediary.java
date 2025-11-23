package me.melontini.andromeda.common.util.commander.number.constant;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;
import net.minecraft.world.level.storage.loot.LootContext;

public record ConstantLongIntermediary(long value) implements LongIntermediary {

  public static final Codec<ConstantLongIntermediary> CODEC =
      Codec.LONG.xmap(ConstantLongIntermediary::new, ConstantLongIntermediary::value);

  @Override
  public long asLong(Supplier<LootContext> supplier) {
    return this.value;
  }
}
