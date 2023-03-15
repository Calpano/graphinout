package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.reader.graphml.GraphmlReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GioEngineCoreTest {

    @Test
    void testServieLoading() {
        GioEngineCore core = new GioEngineCore();
        assertFalse(core.readers().isEmpty());
    }

    /**
     * We read some inputFormat X into GraphML, write GraphML (1), read that GraphML, write to GraphML again (2);
     * compare (1) and (2)
     */
    @Test
    void test() {
//        byte[] graphmlBytes1;
//        {
//            InMemoryOutputSink outputSink = new InMemoryOutputSink();
//            List<ContentError> contentErrors = readResourceToSink(gioReader, resourcePath, outputSink, true, true, true);
//
//            Assertions.assertEquals(expectedErrors.toString(), contentErrors.toString(), "expected=" + expectedErrors + " actual=" + contentErrors);
//            Assertions.assertEquals(expectedErrors, contentErrors, "expected=" + expectedErrors + " actual=" + contentErrors);
//            graphmlBytes1 = outputSink.getByteBuffer().toByteArray();
//        }
//        byte[] graphmlBytes2 = null;
//        {
//            SingleInputSource inputSource = SingleInputSource.of(graphmlBytes1);
//            // TODO read with graphml reader into memory again
//            GioReader graphmlReader = new GraphmlReader();
//            InMemoryOutputSink outputSink = new InMemoryOutputSink();
//            List<ContentError> contentErrors = readTo(inputSource, graphmlReader, outputSink, true, true, true);
//            graphmlBytes2 = outputSink.getByteBuffer().toByteArray();
//        }
//        String graphml1 = new String(graphmlBytes1, StandardCharsets.UTF_8);
//        String graphml2 = new String(graphmlBytes2, StandardCharsets.UTF_8);
//        Assertions.assertEquals(graphml1, graphml2);
    }

}