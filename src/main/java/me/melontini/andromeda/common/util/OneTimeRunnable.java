package me.melontini.andromeda.common.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OneTimeRunnable implements Runnable {

    private Runnable runnable;

    public static OneTimeRunnable of(Runnable runnable) {
        return new OneTimeRunnable(runnable);
    }

    @Override
    public synchronized void run() {
        if (runnable != null) {
            runnable.run();
            runnable = null;
        }
    }
}
