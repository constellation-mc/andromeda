package me.melontini.andromeda.base.util.annotations;

import me.melontini.andromeda.base.util.Environment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {
    String name();
    String category();
    Environment environment() default Environment.BOTH;
}
