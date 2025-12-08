package com.graphinout.foundation.pure.functional;

@FunctionalInterface
public interface ThrowingRunnable {

    static ThrowingRunnable wrap(Runnable runnable) {
        return runnable::run;
    }

    /**
     * @throws Throwable if the implementation throws an exception
     */
    void run() throws Throwable;

}
