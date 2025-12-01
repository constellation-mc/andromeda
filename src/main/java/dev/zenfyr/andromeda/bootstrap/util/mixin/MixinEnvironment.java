package dev.zenfyr.andromeda.bootstrap.util.mixin;

import net.fabricmc.api.EnvType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface MixinEnvironment {
  EnvType value();
}
