package me.melontini.andromeda.util.annotations.config;

import me.melontini.andromeda.util.SharedConstants;
import me.melontini.dark_matter.api.base.util.mixin.Mod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Excluded {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface IfMods {
        Mod[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface IfPlatform {
        SharedConstants.Platform value();
    }

}
