package com.graphinout.foundation.pure.log;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A bad logger that has no dependencies. Logs only if a log sink is registered.
 */
public class LoggerFactory {

    private static final Map<String, Logger> map = new HashMap<>();
    private static Consumer<String> logSink;

    public static Logger getLogger(String name) {
        return map.computeIfAbsent(name, n -> new Logger(name));
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    public static void log(String loggerName, String logLevel, String message) {
        if (logSink != null) {
            logSink.accept(loggerName + " " + logLevel + " " + message);
        }
    }

    public static void logSink(Consumer<String> logSink) {
        LoggerFactory.logSink = logSink;
    }

}
