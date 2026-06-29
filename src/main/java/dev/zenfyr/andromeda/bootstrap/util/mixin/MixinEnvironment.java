package dev.zenfyr.andromeda.bootstrap.util.mixin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import net.fabricmc.api.EnvType;

@Retention(RetentionPolicy.CLASS)
public @interface MixinEnvironment {

  String MIXIN_ENVIRONMENT_ANNOTATION =
      "L" + MixinEnvironment.class.getName().replace(".", "/") + ";";

  EnvType value();
}
