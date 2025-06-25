package com.calpano.graphinout.foundation.json;


import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * SAJ: <em>S</em>AX-like <em>A</em>PI for <em>J</em>SON aka "Streaming API for JSON". Inspired from the XML SAX API.
 * <p>
 * This API can be implemented by readers and writers. The {@link JsonException} allows to wrap I/O issues as well as
 * structural issues in JSON parsing. The API is
 * <ul>
 * <li>precise: no loss of numeric precision, that's why we need double and long;
 * <li>performant: that's why we have int and long as well as float and double -- no conversions required by API for streaming data;
 * <li>small: Higher API layers optionally provide 'luxury' features like the current array index or schema information.
 * </ul>
 * <p>
 * This API reports neither whitespace nor parse location information (line:col).
 *
 * @author xamde
 */
public interface JsonEventStream {

    /**
     * JSON Array
     */
    void arrayEnd() throws JsonException;

    /**
     * JSON Array
     */
    void arrayStart() throws JsonException;

    /**
     * JSON Document
     */
    void documentEnd();

    /**
     * JSON Document
     */
    void documentStart();

    /**
     * JSON Object
     */
    void objectEnd() throws JsonException;

    /**
     * JSON Object
     */
    void objectStart() throws JsonException;

    /**
     * JSON Number
     */
    void onBigDecimal(BigDecimal bigDecimal);

    /**
     * JSON Number
     */
    void onBigInteger(BigInteger bigInteger);

    /**
     * JSON Boolean
     */
    void onBoolean(boolean b) throws JsonException;

    /**
     * JSON Number with decimals, which may be larger than Java {@link Float#MAX_VALUE}
     */
    void onDouble(double d) throws JsonException;

    /**
     * JSON Number with decimals, which is less than or equal to Java {@link Float#MAX_VALUE}
     */
    void onFloat(float f) throws JsonException;

    /**
     * JSON Number without decimals, which is less than or equal to Java {@link Integer#MAX_VALUE}
     */
    void onInteger(int i) throws JsonException;

    /**
     * JSON object property key. Only legal within 'objectStart' and 'objectEnd'
     *
     * @param key the key, without quotes.
     * @throws JsonException if nesting of elements is wrong
     */
    void onKey(String key) throws JsonException;

    /**
     * JSON Number without decimals, which may be larger than Java {@link Integer#MAX_VALUE}
     */
    void onLong(long l) throws JsonException;

    /**
     * JSON Null
     */
    void onNull() throws JsonException;

    /**
     * JSON String
     */
    void onString(String s) throws JsonException;

}
