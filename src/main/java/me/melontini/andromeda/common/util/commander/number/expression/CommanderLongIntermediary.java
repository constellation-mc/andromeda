package me.melontini.andromeda.common.util.commander.number.expression;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;
import me.melontini.commander.api.expression.LongExpression;
import net.minecraft.world.level.storage.loot.LootContext;

@EqualsAndHashCode
@ToString
public final class CommanderLongIntermediary implements LongIntermediary {

  public static final Codec<CommanderLongIntermediary> CODEC = LongExpression.CODEC.xmap(
      CommanderLongIntermediary::new, CommanderLongIntermediary::getArithmetica);

  @Getter
  private final LongExpression arithmetica;

  private final boolean constant;

  public CommanderLongIntermediary(LongExpression arithmetica) {
    this.arithmetica = arithmetica;
    this.constant = arithmetica.toSource().left().isPresent();
  }

  @Override
  public long asLong(Supplier<LootContext> supplier) {
    return this.arithmetica.asLong(constant ? null : supplier.get());
  }
}
