package me.melontini.andromeda.base.util.annotations;

import me.melontini.andromeda.base.util.Environment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SpecialEnvironment {
    Environment value() default Environment.BOTH;
}
