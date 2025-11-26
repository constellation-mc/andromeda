package dev.zenfyr.andromeda.bootstrap;

import dev.zenfyr.andromeda.bootstrap.util.Environment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
  String name();

  String category();

  Environment env() default Environment.BOTH;
}
