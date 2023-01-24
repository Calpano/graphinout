package com.calpano.graphinout.base.reader;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.GraphMlWriter;

public interface GioReader {

    /**
     * Which file format can this reader read?
     * @return
     */
    GioFileFormat fileFormat();

    // inspect

    // validate

    // error reporting

    void read(InputSource inputSource, GraphMlWriter writer);

}
