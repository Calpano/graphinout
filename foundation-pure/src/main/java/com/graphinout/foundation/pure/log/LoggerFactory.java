package com.graphinout.foundation.pure.log;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A bad logger that has no dependencies. Logs only if a log sink is registered.
 * <p>
 * Thread-safe: loggers are created from static initializers of many classes, which the JVM may run on several threads
 * at once. {@code synchronized}/{@code volatile} instead of {@code java.util.concurrent}, so this stays J2CL-compatible
 * (J2CL ignores both keywords).
 */
public class LoggerFactory {

    private static final Map<String, Logger> map = new HashMap<>();
    private static volatile Consumer<String> logSink;

    public static synchronized Logger getLogger(String name) {
        return map.computeIfAbsent(name, n -> new Logger(name));
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    public static void log(String loggerName, String logLevel, String message) {
        Consumer<String> sink = logSink;
        if (sink != null) {
            sink.accept(loggerName + " " + logLevel + " " + message);
        }
    }

    public static void logSink(Consumer<String> logSink) {
        LoggerFactory.logSink = logSink;
    }

}
