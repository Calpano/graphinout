package com.graphinout.reader.gml;

public interface IGmlHandler {


    void key( String key);

    /**
     * @param value includes quotes, if quotes were present.
     */
    void value( String value);

    void open();

    void close();

}
