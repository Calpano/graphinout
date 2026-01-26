package com.graphinout.base.gio;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.output.OutputSink;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * One of the most central interfaces in Graphinout. Defines the contract for reading graph data from a file.
 */
public interface GioWriter {

    Logger _log = getLogger(GioWriter.class);

    /**
     *
     * @param outputSink
     * @return a CJ stream which writes the data send as CJ into the {@link #fileFormat()} in the outputSink
     */
    ICjStream createCjStream(OutputSink outputSink);

    /**
     * Which file format can this writer write?
     */
    GioFileFormat fileFormat();

    default void writeCjDocument(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        ICjStream cjStream = createCjStream(outputSink);
        CjWriter2CjStream cjWriter2Stream = new CjWriter2CjStream(cjStream);
        // IMPROVE get this param from outside
        boolean sort = true;
        cjDoc.fire(cjWriter2Stream, sort);
    }

}
