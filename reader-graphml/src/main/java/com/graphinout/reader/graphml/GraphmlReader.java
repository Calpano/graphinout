package com.graphinout.reader.graphml;

import com.graphinout.foundation.pure.input.BaseOutput;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.base.input.ContentErrors;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.xml.sax.Sax2XmlWriter;
import com.graphinout.base.xml.sax.SimpleSaxErrorHandler;
import com.graphinout.base.xml.factory.XmlFactory;
import com.graphinout.base.xml.util.XmlTool;
import com.graphinout.reader.graphml.cj.Graphml2CjWriter;
import org.slf4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.jspecify.annotations.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlReader extends BaseOutput implements GioReader {

    public static final String FORMAT_ID = "graphml";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "GraphML",  ".graphml.xml", ".graphml");

    private static final Logger log = getLogger(GraphmlReader.class);

    /**
     * TODO This can load from config file - use only GraphML 1.1
     * lists of schema contents.
     * <p>
     * The schema map is the result of a full ClassGraph classpath scan for {@code *.xsd.xml} resources. That scan is
     * expensive and its result never varies per input document, so it is computed once per JVM and shared (thread-safe,
     * lazily, via the {@link SchemaCache} holder) across all reader instances and concurrent reads.
     */
    private final Map<String, String> externalSchemaMap;

    public GraphmlReader() {
        Map<String, String> map;
        try {
            map = SchemaCache.externalSchemaMap();
        } catch (IOException e) {
            sendContentError_Warn("Schema loading failed", e);
            map = Map.of();
        }
        this.externalSchemaMap = map;
    }


    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    public @Nullable String getSchema(String localSchemaResourceName) {
        return externalSchemaMap.get(localSchemaResourceName);
    }

    @Override
    public boolean isValid(InputSource inputSource) throws IOException {
        if (inputSource.isMulti()) return false;
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        try {
            validateWellFormed(singleInputSource);
            //TODO There is no internal DTD in any of the files that have been processed so far.
            //If it exists, it can be verified with this method
            //validateInternalDTD(singleInputSource);
            //ValidateInternalXSD(singleInputSource);
            validateExternalSchema(singleInputSource);
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
        //TODO This method does not work well here
        return GioReader.super.isValid(singleInputSource);
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        if (inputSource.isMulti()) {
            throw sendContentError_Error(null, new IllegalArgumentException("MultiInputSource is not supported by GraphmlReader"), null);
        }
        SingleInputSource singleInputSource = ((SingleInputSource) inputSource);

        CjWriter2CjStream cjWriter2CjStream = new CjWriter2CjStream(writer);
        Graphml2CjWriter graphml2CjWriter = new Graphml2CjWriter(cjWriter2CjStream);
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2CjWriter);

        // fetch readers error handler and propagate it down the chain
        Consumer<ContentError> contentErrorHandler = super.contentErrorHandler();
        xml2GraphmlWriter.setContentErrorHandler(contentErrorHandler);
        Sax2XmlWriter saxHandler = new Sax2XmlWriter(xml2GraphmlWriter);
        try {
            try {
                XMLReader reader = XmlTool.createXmlReaderOn(saxHandler);
                try {
                    InputStream in = singleInputSource.inputStream();
                    org.xml.sax.InputSource saxInputSource = new org.xml.sax.InputSource(in);
                    reader.parse(saxInputSource);
                } catch (SAXException e) {
                    throw sendContentError_Error(null, e, null);
                }
            } catch (ParserConfigurationException e) {
                throw sendContentError_Error(null, e, null);
            }
        } catch (ContentErrorException t) {
            log.warn("ContentError", t);
        } catch (Throwable t) {
            throw new RuntimeException("Failed reading '" + inputSource.name(), t);
        }
    }


    /**
     * Two sub-options: (1) XML file contains "xsi:schemaLocation" -> take XML Schema from there; (2) we pre-downloaded
     * Graphml.XSD, use it, and ignore "xsi:schemaLocation" -- <a
     * href="http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd">graphml 1.1 XSD</a>
     *
     * @param inputSource to validate
     */
    void validateExternalSchema(SingleInputSource inputSource) throws SAXException, IOException, ParserConfigurationException {
        if (externalSchemaMap.isEmpty())
            throw sendContentError_Error(null, new IllegalStateException("no schemas loaded"), null);
        if (externalSchemaMap.get("graphml.xsd.xml") == null) {
            throw sendContentError_Error(null, new IllegalStateException("Required schema 'graphml.xsd.xml' not loaded"), null);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        // The compiled GraphML 1.1 schema is immutable and thread-safe; it is built once per JVM (see SchemaCache) and
        // reused here, instead of re-scanning the classpath and re-compiling the XSD on every read.
        try {
            factory.setSchema(SchemaCache.graphmlSchema());
        } catch (SAXException e) {
            throw sendContentError_Error(null, e, null);
        }

        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleSaxErrorHandler(ContentErrors.SIMPLE_LOGGING));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

    void validateInternalDTD(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleSaxErrorHandler(ContentErrors.SIMPLE_LOGGING));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

    void validateInternalXSD(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();
        parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleSaxErrorHandler(ContentErrors.SIMPLE_LOGGING));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

    void validateWellFormed(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = XmlFactory.createSaxParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleSaxErrorHandler(ContentErrors.SIMPLE_LOGGING));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

}
