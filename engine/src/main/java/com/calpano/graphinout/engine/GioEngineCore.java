package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.file.SimpleXmlWriter;
import com.calpano.graphinout.base.reader.ContentErrors;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.reader.InMemoryContentHandler;
import com.calpano.graphinout.reader.dot.DotTextReader;
import com.calpano.graphinout.reader.graphml.GraphmlReader;
import com.calpano.graphinout.reader.tgf.TgfReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GioEngineCore {

    private final List<GioReader> readers = new ArrayList<>();

    public GioEngineCore() {
        // TODO find available GioReader impl via service loader
        readers.add(new TgfReader());
        readers.add(new DotTextReader());
        readers.add(new GraphmlReader());
    }

    public void read(SingleInputSource inputSource, OutputSink outputSink) throws IOException {
        List<GioReader> readerCandidates = selectReaders(inputSource);
        // we expect only one of them to be able to read it without throwing exceptions or reporting contentErrors
        OutputSink noop = OutputSink.createNoop();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new SimpleXmlWriter(noop)));
        for(GioReader reader: readerCandidates) {
            InMemoryContentHandler errorHandler = ContentErrors.createInMemory();
            reader.errorHandler(errorHandler);
           if (reader.isValid(inputSource)) {
               reader.read(inputSource, gioWriter);
           }
           if(errorHandler.hasNoErrors()) {
              // parsing worked, now for real
               gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new SimpleXmlWriter(outputSink)));
               return;
           }
        }
        throw new IllegalStateException("Could not find any parser able to parse "+inputSource.name());
    }

    private List<GioReader> selectReaders(SingleInputSource inputSource) {
        // find reader candidates based on inputSource.inputFormat() and reader.fileFormat()
        // TODO let fileFormat have registered list of extensions and use for matching
        // fake it
        return readers;
    }

}
