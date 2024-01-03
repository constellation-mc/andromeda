package me.melontini.andromeda.util.exceptions;

import me.melontini.andromeda.util.Debug;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class MixinVerifyError extends Error {

    private final Set<Complaint> complaints;
    private final String cls;

    private MixinVerifyError() {
        this("Invalid constructor called!", Collections.emptySet());
    }

    private MixinVerifyError(String cls, Set<Complaint> complaints) {
        super("Mixin verification failed.");
        this.cls = cls;
        this.complaints = complaints;
    }

    @Override
    public String getMessage() {
        StringBuilder b = new StringBuilder();
        b.append("(Andromeda) ").append(super.getMessage()).append(" class: ").append(cls);

        if (!complaints.isEmpty()) {
            complaints.forEach(c -> b.append("\n\t").append("- ").append(c.message));
        }

        b.append('\n').append("If you do not wish to see these error, please remove '%s' from your 'andromeda/debug.json'".formatted(Debug.Keys.VERIFY_MIXINS));
        return b.toString();
    }

    public static class Builder {

        private final Set<Complaint> complaints = new LinkedHashSet<>();
        private final String cls;

        public Builder(String cls) {
            this.cls = cls;
        }

        public Builder complaint(String message) {
            complaints.add(new Complaint(message));
            return this;
        }

        public boolean isEmpty() {
            return complaints.isEmpty();
        }

        public MixinVerifyError build() {
            return new MixinVerifyError(cls, complaints);
        }
    }

    public record Complaint(String message) {
    }
}
