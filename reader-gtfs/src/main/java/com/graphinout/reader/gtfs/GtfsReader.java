package com.graphinout.reader.gtfs;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Reads GTFS (General Transit Feed Specification) feeds, see <a href="https://gtfs.org/">gtfs.org</a>.
 * <p>
 * A GTFS feed is a ZIP archive of CSV files ({@code stops.txt}, {@code routes.txt}, {@code trips.txt},
 * {@code stop_times.txt}, ...). This reader accepts either the ZIP as a {@link com.graphinout.base.input.SingleInputSource}
 * or the unpacked files as a {@link com.graphinout.base.input.MultiInputSource} with the GTFS file names as source names.
 * <p>
 * The result is a <b>two-level CJ model</b>: a base graph of the physical world (stations + platforms as nodes,
 * transfers as the only edges) followed by one subgraph per route whose directed travel edges reference the base
 * graph's platform nodes by id. See {@link Gtfs2Cj} for the full mapping.
 */
public class GtfsReader implements GioReader {

    public static final String FORMAT_ID = "gtfs";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, //
            "GTFS - General Transit Feed Specification", ".gtfs.zip", ".gtfs");

    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parseGtfsToCjDocument(InputSource inputSource) throws IOException {
        GtfsReader reader = new GtfsReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2cj = new CjStream2CjWriter(cj2document, true);
        reader.read(inputSource, cjStream2cj);
        return cj2document.resultDoc();
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        GtfsFiles files = GtfsFiles.of(inputSource);
        new Gtfs2Cj(files, errorHandler).read(cjStream);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }
}
