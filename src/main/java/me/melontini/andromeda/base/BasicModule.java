package me.melontini.andromeda.base;

import me.melontini.andromeda.config.BasicConfig;

public interface BasicModule extends Module<BasicConfig> {

    @Override
    default Class<BasicConfig> configClass() {
        return BasicConfig.class;
    }
}
