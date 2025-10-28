package com.graphinout.foundation.json.stream.impl;

import com.graphinout.base.BaseOutput;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.stream.JsonWriter;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.slf4j.LoggerFactory.getLogger;

public class LoggingJsonWriter extends BaseOutput implements JsonWriter {

    public enum Output {Log, SystemOut}

    private static final Logger log = getLogger(LoggingJsonWriter.class);
    private final Output output;

    public LoggingJsonWriter() {
        this(Output.Log);
    }

    public LoggingJsonWriter(Output output) {
        this.output = output;
    }

    @Override
    public void arrayEnd() throws JsonException {
        output("arrayEnd");
    }

    @Override
    public void arrayStart() throws JsonException {
        output("arrayStart");
    }

    @Override
    public void documentEnd() {
        output("documentEnd");
    }

    @Override
    public void documentStart() {
        output("documentStart");
    }

    @Override
    public void objectEnd() throws JsonException {
        output("objectEnd");
    }

    @Override
    public void objectStart() throws JsonException {
        output("objectStart");
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        output("onBigDecimal: {}", bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigIntegerValue) {
        output("onBigInteger: {}", bigIntegerValue);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        output("onBoolean: {}", b);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        output("onDouble: {}", d);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        output("onFloat: {}", f);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        output("onInteger: {}", i);
    }

    @Override
    public void onKey(String key) throws JsonException {
        output("onKey: '{}'", key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        output("onLong: {}", l);
    }

    @Override
    public void onNull() throws JsonException {
        output("onNull");
    }

    @Override
    public void onString(String s) throws JsonException {
        output("string: '{}'", s);
    }

    private void output(String s, Object... o) {
        switch (output) {
            case Log:
                log.info(s, o);
                break;
            case SystemOut:
                String pattern = s.replace("{}", "%s");
                System.out.printf("[" + pattern + "]", o);
                break;
        }
    }

}
