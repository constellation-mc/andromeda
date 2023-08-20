package me.melontini.andromeda.util.annotations.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureEnvironment {

    Environment value() default Environment.BOTH;

}
