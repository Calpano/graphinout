package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.calpano.graphinout.base.graphml.Graphml2XmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.calpano.graphinout.foundation.TestFileUtil.inputSource;

class GraphmlReaderTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    void readAllGraphmlFiles(String displayName, Resource resource) throws Exception {
        try (SingleInputSource singleInputSource = inputSource(resource)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(Xml2AppendableWriter.createNoop()));
            graphmlReader.read(singleInputSource, gioWriter);
        }
    }

}
