package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.ContentErrors;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.slf4j.LoggerFactory.getLogger;

class GioEngineCoreTest {

    static final File reportResultDir = new File("./target/parse-reports");
    private static final Logger log = getLogger(GioEngineCoreTest.class);
    private static GioEngineCore gioEngineCore;

    @BeforeAll
    static void beforeAll() {
        gioEngineCore = new GioEngineCore();
        if (!reportResultDir.exists())
            reportResultDir.mkdirs();
    }

    /**
     * We read some inputFormat X into GraphML, write GraphML (1), read that GraphML, write to GraphML again (2);
     * compare (1) and (2)
     */
    @Test
    @Disabled("needs to fix the generic JsonReader")
    void test() {
        // find all resources
        ReaderTests.getAllTestResourceFilePaths().forEach(this::testResource);


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

    @Test
    void testWith1Resource() {
        testResource("graphin/graphml/synthetic/invalid-root.graphml");
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
            if (ContentErrors.hasErrors(contentErrors)) {
                log.warn("Resource '{}' interpreted as '{}' contentErrors: {}", resourcePath, gioReader.fileFormat().id(), contentErrors);
                writeContentErrorsFile(contentErrors, gioReader.fileFormat().id(), resourcePath);
            } else {
                // reading likely worked
                log.info("Resource '{}' interpreted as '{}' read to GraphML as {} bytes", resourcePath, gioReader.fileFormat().id(), outputSink.getByteBuffer().size());
                File resultFile = new File(reportResultDir, gioReader.fileFormat().id() + "/" + resourcePath + ".graphml");
                resultFile.getParentFile().mkdirs();
                byte[] bytes = outputSink.getByteBuffer().toByteArray();
                FileUtils.writeByteArrayToFile(resultFile, bytes);
                if (!contentErrors.isEmpty()) {
                    writeContentErrorsFile(contentErrors, gioReader.fileFormat().id(), resourcePath);
                }
            }
        } catch (RuntimeException e) {
            e.fillInStackTrace();
            log.warn("Resource '{}' interpreted as '{}' parse exception: {}", resourcePath, gioReader.fileFormat().id(), e);
            File resultFile = new File(reportResultDir, gioReader.fileFormat().id() + "/" + resourcePath + ".parse-errors.txt");
            resultFile.getParentFile().mkdirs();
            StringBuilder b = new StringBuilder();
            b.append("# Generated on " + LocalDateTime.now() + "\n");
            b.append("Parsed " + resourcePath + " as " + gioReader.fileFormat().id() + "\n");
            b.append("--\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String ex = sw.getBuffer().toString();
            b.append(ex);
            FileUtils.writeStringToFile(resultFile, b.toString(), StandardCharsets.UTF_8);
        }
    }

    private void writeContentErrorsFile(List<ContentError> contentErrors, String fileFormatId, String resourcePath) throws IOException {
        File resultFile = new File(reportResultDir, fileFormatId + "/" + resourcePath + ".content-errors.txt");
        resultFile.getParentFile().mkdirs();
        List<String> lines = new ArrayList<>();
        lines.add("# Generated on " + LocalDateTime.now());
        lines.add("Parsed " + resourcePath + " as " + fileFormatId);
        lines.add("Found " + contentErrors.size() + " content errors.");
        if (ContentErrors.hasErrors(contentErrors)) {
            lines.add("!!! Found errors.");
        }
        lines.add("--");
        for (ContentError ce : contentErrors) {
            lines.add(ce.getLevel() + ": " + ce.getMessage() + " @" + ce.getLocation());
        }
        FileUtils.writeLines(resultFile, lines);
    }

}
