package dev.zenfyr.andromeda.common.util;

import java.util.Optional;

public interface TranslationKeyProvider {
  default Optional<String> getTranslationKey() {
    return Optional.empty();
  }
}
