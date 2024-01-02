package me.melontini.andromeda.base.annotations;

import me.melontini.andromeda.common.config.ScopedConfigs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on modules or config options, which don't support {@link ScopedConfigs}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Unscoped {
}
