package me.melontini.andromeda.base.annotations;

import me.melontini.andromeda.base.MixinProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MixinConfig {
    Class<?> plugin() default MixinProcessor.Plugin.class;
    String refmap() default "andromeda-refmap.json";
}
