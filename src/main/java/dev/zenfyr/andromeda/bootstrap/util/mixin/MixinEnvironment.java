package dev.zenfyr.andromeda.bootstrap.util.mixin;

import net.fabricmc.api.EnvType;

public @interface MixinEnvironment {
  EnvType value();
}
