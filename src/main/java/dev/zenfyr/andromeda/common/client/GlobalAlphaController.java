package dev.zenfyr.andromeda.common.client;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

public class GlobalAlphaController {

  public static final ThreadLocal<Float2FloatFunction> MODIFIER =
      ThreadLocal.withInitial(() -> null);
}
