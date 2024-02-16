package me.melontini.andromeda.base.util.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Origin {
    String mod();

    String author();
}
