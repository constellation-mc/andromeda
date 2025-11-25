package dev.zenfyr.andromeda.bootstrap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import dev.zenfyr.andromeda.bootstrap.util.Environment;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
  String name();

  String category();

  Environment env() default Environment.BOTH;
}
