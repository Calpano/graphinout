package com.graphinout.base.gio;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

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

    /**
     * Optional sink for write-time content problems — e.g. graph data the target format cannot represent
     * (hyper-edges, nodes without ids). No-op by default; writers that can lose data on output override this and
     * forward it to their format generator.
     */
    default void setContentErrorHandler(Consumer<ContentError> contentErrorHandler) {
        // no-op by default
    }

    default void writeCjDocument(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        ICjStream cjStream = createCjStream(outputSink);
        CjWriter2CjStream cjWriter2Stream = new CjWriter2CjStream(cjStream);
        // IMPROVE get this param from outside
        boolean sort = true;
        cjDoc.fire(cjWriter2Stream, sort);
    }

}
