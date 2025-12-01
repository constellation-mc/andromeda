package dev.zenfyr.andromeda.bootstrap.util.mixin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import net.fabricmc.api.EnvType;

@Retention(RetentionPolicy.CLASS)
public @interface MixinEnvironment {
  EnvType value();
}
