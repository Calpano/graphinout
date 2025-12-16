package com.graphinout.foundation.pure.log;

import com.graphinout.foundation.pure.bridge.Java9;

public class Logger {

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public void debug(String template, Object... data) {
        render("DEBUG", template, data);
    }

    public void error(String template, Object... data) {
        render("ERROR", template, data);
    }

    public void info(String template, Object... data) {
        render("INFO", template, data);
    }

    public void trace(String template, Object... data) {
        render("TRACE", template, data);
    }

    public void warn(String template, Object... data) {
        render("WARN", template, data);
    }

    private void render(String level, String template, Object[] data) {
        // TODO better
        String message = level + " " + template + " DATA:" + Java9.List.of(data).toString();
        LoggerFactory.log(name, level, message);
    }

}
