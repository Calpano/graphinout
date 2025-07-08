package com.calpano.graphinout.foundation.json;


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
public interface JsonWriter extends JsonElementWriter {


    /**
     * JSON Document
     */
    void documentEnd() throws JsonException;

    /**
     * JSON Document
     */
    void documentStart() throws JsonException;



}
