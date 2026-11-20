package com.graphinout.engine.doc;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.gio.GioInputAnalysis;
import com.graphinout.base.gio.GioInputAnalyzer;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.FileOutputSink;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.engine.GioEngineCore;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.tgf.TgfReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;

/**
 * Runnable examples for {@code doc/usage.adoc}. Each {@code tag::name[]} region is copied verbatim into the doc;
 * {@link DocSnippetsTest} fails when the two drift apart.
 */
class ApiUsageExamplesTest {

    private static final String TGF = "1 Alice\n2 Bob\n#\n1 2 knows\n";

    /** The short example in README.adoc. */
    @Test
    void readmeExample(@TempDir Path tmp) throws IOException {
        String tgfText = TGF;
        File outputFile = tmp.resolve("example.graphml").toFile();
        // tag::readme[]
        GioEngineCore engine = new GioEngineCore();
        ICjDocument cjDoc = engine.getReader("tgf").readToCjDocument(SingleInputSource.of("example.tgf", tgfText));
        engine.getWriter("graphml").writeCjDocument(cjDoc, new FileOutputSink(outputFile));
        // end::readme[]

        assertThat(Files.readString(outputFile.toPath())).contains("Alice");
    }

    @Test
    void convertInMemory() throws IOException {
        // tag::convert[]
        GioEngineCore engine = new GioEngineCore(); // finds all readers & writers on the classpath
        GioReader reader = engine.getReader("tgf");
        GioWriter writer = engine.getWriter("graphml");

        SingleInputSource input = SingleInputSource.of("example.tgf", "1 Alice\n2 Bob\n#\n1 2 knows\n");
        ICjDocument cjDoc = reader.readToCjDocument(input);

        InMemoryOutputSink output = InMemoryOutputSink.create();
        writer.writeCjDocument(cjDoc, output);
        String graphml = output.getBufferAsUtf8String();
        // end::convert[]

        assertThat(cjDoc.nodesAll().count()).isEqualTo(2);
        assertThat(graphml).contains("<graphml");
        assertThat(graphml).contains("Alice");
    }

    @Test
    void convertStreaming() throws IOException {
        GioEngineCore engine = new GioEngineCore();
        // tag::stream[]
        GioReader reader = engine.getReader("tgf");
        GioWriter writer = engine.getWriter("connected-json");

        InMemoryOutputSink output = InMemoryOutputSink.create();
        // the reader pushes CJ events straight into the writer -- no in-memory document is built
        reader.read(SingleInputSource.of("example.tgf", TGF), writer.createCjStream(output));
        // end::stream[]

        assertThat(output.getBufferAsUtf8String()).contains("Alice");
    }

    @Test
    void contentErrors() throws IOException {
        // tag::errors[]
        GioReader reader = new TgfReader(); // readers can also be used without the engine
        List<ContentError> errors = new ArrayList<>();
        reader.setContentErrorHandler(errors::add);

        ICjDocument cjDoc = reader.readToCjDocument(SingleInputSource.of("broken.tgf", "1 Alice\n#\n1\n"));
        for (ContentError error : errors) {
            System.out.println(error.level + " at " + error.location + ": " + error.message);
        }
        // end::errors[]

        assertThat(errors).isNotEmpty();
        assertThat(cjDoc).isNotNull();
    }

    @Test
    void detectFormat() throws IOException {
        GioEngineCore engine = new GioEngineCore();
        // tag::detect[]
        GioInputAnalyzer analyzer = new GioInputAnalyzer(engine::readers);
        GioInputAnalysis analysis = analyzer.analyze(SingleInputSource.of("unknown.txt", "digraph { a -> b }"));

        Optional<GioInputAnalysis.Candidate> best = analysis.best();
        best.ifPresent(c -> System.out.println(c.format().id() + " (" + analysis.tier() + "): " + c.explanation()));
        // end::detect[]

        assertThat(best.map(c -> c.format().id())).hasValue("dot");
    }

    @Test
    void listFormats() {
        GioEngineCore engine = new GioEngineCore();
        // tag::formats[]
        engine.formats().forEach((format, support) ->
                System.out.println(format.id() + "\t" + support + "\t" + format.fileExtensions()));
        // end::formats[]

        assertThat(engine.formats()).isNotEmpty();
    }

}
