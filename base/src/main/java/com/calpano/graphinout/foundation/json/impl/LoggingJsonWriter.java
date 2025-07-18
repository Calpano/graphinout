package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.JsonWriter;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.slf4j.LoggerFactory.getLogger;

public class LoggingJsonWriter implements JsonWriter {

    private static final Logger log = getLogger(LoggingJsonWriter.class);

    @Override
    public void arrayEnd() throws JsonException {
        log.info("arrayEnd");
    }

    @Override
    public void arrayStart() throws JsonException {
        log.info("arrayStart");
    }

    @Override
    public void documentEnd() {
        log.info("documentEnd");
    }

    @Override
    public void documentStart() {
        log.info("documentStart");
    }

    @Override
    public void objectEnd() throws JsonException {
        log.info("objectEnd");
    }

    @Override
    public void objectStart() throws JsonException {
        log.info("objectStart");
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        log.info("onBigDecimal: {}", bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigIntegerValue) {
        log.info("onBigInteger: {}", bigIntegerValue);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        log.info("onBoolean: {}", b);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        log.info("onDouble: {}", d);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        log.info("onFloat: {}", f);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        log.info("onInteger: {}", i);
    }

    @Override
    public void onKey(String key) throws JsonException {
        log.info("onKey: {}", key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        log.info("onLong: {}", l);
    }

    @Override
    public void onNull() throws JsonException {
        log.info("onNull");
    }

    @Override
    public void stringCharacters(String s) throws JsonException {
        log.info("stringCharacters: '{}'", s);
    }

    @Override
    public void stringEnd() throws JsonException {
        log.info("stringEnd");
    }

    @Override
    public void stringStart() throws JsonException {
        log.info("stringStart");
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        log.info("whitespaceCharacters: {}", s);
    }

}
