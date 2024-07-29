package me.melontini.andromeda.base.util.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import me.melontini.andromeda.base.util.Environment;

@Retention(RetentionPolicy.RUNTIME)
public @interface SpecialEnvironment {
  Environment value() default Environment.BOTH;
}
