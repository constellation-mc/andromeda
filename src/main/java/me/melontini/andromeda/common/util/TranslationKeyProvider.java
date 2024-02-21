package me.melontini.andromeda.common.util;

import java.util.Optional;

public interface TranslationKeyProvider {
    default Optional<String> getTranslationKey() {
        return Optional.empty();
    }
}
