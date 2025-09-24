package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class GraphmlReaderTest {

    private static final Logger log = LoggerFactory.getLogger(GraphmlReaderTest.class);

    @ParameterizedTest
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlResources")
    void readAllGraphmlFiles(String filePath) throws Exception {
        log.info("Start To pars file [{}]", filePath);
        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(filePath, content)
        ) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(Xml2AppendableWriter.createNoop()));
            graphmlReader.read(singleInputSource, gioWriter);
        }
    }

}
