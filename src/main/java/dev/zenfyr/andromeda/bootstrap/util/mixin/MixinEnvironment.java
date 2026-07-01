package dev.zenfyr.andromeda.bootstrap.util.mixin;

import dev.zenfyr.pulsar.api.platform.CEnvType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface MixinEnvironment {

  String MIXIN_ENVIRONMENT_ANNOTATION =
      "L" + MixinEnvironment.class.getName().replace(".", "/") + ";";

  CEnvType value();
}
