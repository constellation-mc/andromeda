package me.melontini.andromeda.util.exceptions;

public class MixinVerifyError extends Error {

    public MixinVerifyError(String msg) {
        super(msg);
    }

    public MixinVerifyError(String msg, Throwable cause) {
        super(msg, cause);
    }
}
