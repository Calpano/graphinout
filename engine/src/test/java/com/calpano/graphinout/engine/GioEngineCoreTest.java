package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.reader.ContentErrors;
import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.slf4j.LoggerFactory.getLogger;

class GioEngineCoreTest {

    private static final Logger log = getLogger(GioEngineCoreTest.class);

    private static GioEngineCore gioEngineCore;

    @BeforeAll
    static void beforeAll() {
        gioEngineCore = new GioEngineCore();
        if(!reportResultDir.exists())
            reportResultDir.mkdirs();
    }

    static final File reportResultDir = new File("./target/parse-reports");

    /**
     * We read some inputFormat X into GraphML, write GraphML (1), read that GraphML, write to GraphML again (2);
     * compare (1) and (2)
     */
    @Test
    void test() {
        // find all resources
        ReaderTests.getAllTestResourceFilePaths().forEach(r -> testResource(r));


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

    @Test
    void testServiceLoading() {
        GioEngineCore core = new GioEngineCore();
        assertFalse(core.readers().isEmpty());
    }

    private void testResource(String resourcePath) {
        URL resourceUrl = ClassLoader.getSystemResource(resourcePath);
        if (gioEngineCore.canRead(resourcePath)) {
            log.info("Reading " + resourceUrl + " in memory");
            try {
                String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
                SingleInputSource inputSource = SingleInputSource.of(resourcePath, content);
                for (GioReader gioReader : gioEngineCore.readers()) {
                    if (gioReader.fileFormat().matches(resourcePath)) {
                        testResourceWithReader(gioReader, inputSource, resourcePath);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void testResourceWithReader(GioReader gioReader, SingleInputSource inputSource, String resourcePath) throws IOException {
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        boolean validateXml = true;
        boolean validateGraphml = true;
        boolean validateGio = true;
        try {
            List<ContentError> contentErrors = ReaderTests.readTo(inputSource, gioReader, outputSink, validateXml, validateGraphml, validateGio);
            if(ContentErrors.hasErrors(contentErrors)) {
                log.warn("Resource '{}' interpreted as '{}' contentErrors: {}", resourcePath, gioReader.fileFormat().id(), contentErrors);
            } else {
                // reading likely worked
                log.info("Resource '{}' interpreted as '{}' read to GraphML as {} bytes", resourcePath, gioReader.fileFormat().id(), outputSink.getByteBuffer().size());
                File resultFile = new File(reportResultDir, gioReader.fileFormat().id()+"/"+resourcePath+".graphml" );
                resultFile.getParentFile().mkdirs();
                byte[] bytes = outputSink.getByteBuffer().toByteArray();
                FileUtils.writeByteArrayToFile(resultFile, bytes);
            }
        } catch (RuntimeException e) {
            e.fillInStackTrace();
            log.warn("Resource '{}' interpreted as '{}' parse exception: {}", resourcePath, gioReader.fileFormat().id(), e);
        }
    }

}