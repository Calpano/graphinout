package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.foundation.util.Nullables;

public class Graphml2CjWriter extends Graphml2CjDocument implements GraphmlWriter {

    private final ICjWriter cjWriter;

    public Graphml2CjWriter(ICjWriter cjWriter) {
        this.cjWriter = cjWriter;
    }

    @Override
    public void documentEnd() {
        Nullables.ifPresentAccept(resultDoc(), doc->doc.fire(cjWriter));
    }

}
