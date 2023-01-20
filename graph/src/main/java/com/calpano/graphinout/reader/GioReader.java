package com.calpano.graphinout.reader;

import com.calpano.graphinout.input.InputSource;
import com.calpano.graphinout.output.GraphMlWriter;

public interface GioReader {

    /**
     * Which file format can this reader read?
     * @return
     */
    GioFileformat fileFormat();



    // inspect

    // validate

    // error reporting

    void read(InputSource inputSource, GraphMlWriter writer);

}
