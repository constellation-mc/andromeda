package me.melontini.andromeda.common.util.commander;

import com.google.gson.GsonBuilder;
import me.melontini.andromeda.common.util.GsonCodecContext;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.common.util.commander.bool.CommanderBooleanIntermediary;
import me.melontini.andromeda.common.util.commander.bool.ConstantBooleanIntermediary;
import me.melontini.andromeda.common.util.commander.number.DoubleIntermediary;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;
import me.melontini.andromeda.common.util.commander.number.constant.ConstantDoubleIntermediary;
import me.melontini.andromeda.common.util.commander.number.constant.ConstantLongIntermediary;
import me.melontini.andromeda.common.util.commander.number.expression.CommanderDoubleIntermediary;
import me.melontini.andromeda.common.util.commander.number.expression.CommanderLongIntermediary;
import me.melontini.dark_matter.api.base.util.Support;

public class IntermediaryTypes {

  public static void initialize(GsonBuilder builder) {
    builder.registerTypeHierarchyAdapter(
        DoubleIntermediary.class,
        GsonCodecContext.of(Support.fallback(
            "commander",
            () -> CommanderDoubleIntermediary.CODEC,
            () -> ConstantDoubleIntermediary.CODEC)));

    builder.registerTypeHierarchyAdapter(
        LongIntermediary.class,
        GsonCodecContext.of(Support.fallback(
            "commander",
            () -> CommanderLongIntermediary.CODEC,
            () -> ConstantLongIntermediary.CODEC)));

    builder.registerTypeHierarchyAdapter(
        BooleanIntermediary.class,
        GsonCodecContext.of(Support.fallback(
            "commander",
            () -> CommanderBooleanIntermediary.CODEC,
            () -> ConstantBooleanIntermediary.CODEC)));
  }
}
