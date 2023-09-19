package me.melontini.andromeda.api.config;

public interface TextEntry {

    String DEFAULT_KEY = "andromeda.config.tooltip.manager.";

    static TextEntry literal(String literal) {
        return new TextEntry() {
            @Override
            public String text() {
                return literal;
            }

            @Override
            public Object[] args() {
                return null;
            }

            @Override
            public boolean isTranslatable() {
                return false;
            }
        };
    }

    static TextEntry translatable(String key, Object... args) {
        return new TextEntry() {
            @Override
            public String text() {
                return key;
            }

            @Override
            public Object[] args() {
                return args;
            }

            @Override
            public boolean isTranslatable() {
                return true;
            }
        };
    }

    String text();

    Object[] args();

    boolean isTranslatable();
}
