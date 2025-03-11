package me.melontini.andromeda.bootstrap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import me.melontini.andromeda.bootstrap.util.Environment;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
  String name();

  String category();

  Environment env() default Environment.BOTH;
}
