package com.graphinout.reader.graphml;

import com.graphinout.base.cj.stream.DelegatingCjStream;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.reader.graphml.validation.ValidatingGraphMlWriter;
import com.graphinout.base.cj.stream.ValidatingCjStream;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.output.OutputSink;
import com.graphinout.foundation.xml.writer.ValidatingXmlWriter;
import com.graphinout.foundation.xml.writer.Xml2AppendableWriter;
import com.graphinout.foundation.xml.writer.XmlWriter;
import com.graphinout.foundation.xml.writer.XmlWriterImpl;
import com.graphinout.reader.graphml.cj.CjStream2GraphmlWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.graphinout.foundation.TestFileUtil.inputSource;
import static org.junit.jupiter.api.Assertions.fail;

class GraphmlReaderTest {

    public static ICjStream createCjStream(OutputSink outputSink, boolean validateXml, boolean validateGraphml, boolean validateGio) throws IOException {
        XmlWriter xmlWriter = XmlWriterImpl.create(outputSink);
        if (validateXml) {
            xmlWriter = new ValidatingXmlWriter(xmlWriter);
        }

        GraphmlWriter graphmlWriter = new Graphml2XmlWriter(xmlWriter);
        if (validateGraphml) {
            graphmlWriter = new DelegatingGraphmlWriter(new ValidatingGraphMlWriter(), graphmlWriter);
        }


        ICjStream cjStream = new CjStream2GraphmlWriter(graphmlWriter);
        if (validateGio) {
            cjStream = new DelegatingCjStream(new ValidatingCjStream(), cjStream);
        }
        return cjStream;
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#graphmlResources")
    void readAllGraphmlFiles(String displayName, Resource resource) throws Exception {
        try (SingleInputSource singleInputSource = inputSource(resource)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.setContentErrorHandler(contentErrors::add);

            Graphml2XmlWriter graphmlWriter = new Graphml2XmlWriter(Xml2AppendableWriter.createNoop());
            ICjStream cjStream = new CjStream2GraphmlWriter(graphmlWriter);
            boolean isInvalid = TestFileUtil.isInvalid(resource, "xml", "graphml");
            try {
                graphmlReader.read(singleInputSource, cjStream);
                if (isInvalid) {
                    fail("Invalid resource should have thrown");
                }
            } catch (Throwable t) {
                if (isInvalid) {
                    assertWithMessage("Found no contentErrors although we got: " + t.getMessage()).that(contentErrors).isNotEmpty();
                } else {
                    fail("Unexpected error on valid file", t);
                }
            }
        }
    }


}
