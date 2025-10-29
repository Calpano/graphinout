package com.graphinout.reader.graphml;

import com.graphinout.foundation.input.BaseOutput;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.ContentErrorException;
import com.graphinout.foundation.input.ContentErrors;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.xml.sax.Sax2XmlWriter;
import com.graphinout.foundation.xml.sax.SimpleSaxErrorHandler;
import com.graphinout.foundation.xml.factory.XmlFactory;
import com.graphinout.foundation.xml.util.XmlTool;
import com.graphinout.reader.graphml.cj.Graphml2CjWriter;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import org.slf4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlReader extends BaseOutput implements GioReader {

    public static final String FORMAT_ID = "graphml";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "GraphML",  ".graphml.xml", ".graphml");

    private static final Logger log = getLogger(GraphmlReader.class);
    /**
     * TODO This can load from config file - use only GraphML 1.1
     * lists of schema contents
     */
    private final Map<String, String> externalSchemaMap = new HashMap<>();

    public GraphmlReader() {
        try {
            loadSchemaFiles();
        } catch (IOException e) {
            sendContentError_Warn("Schema loading failed", e);
        }
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
            throw sendContentError_Error("--", new IllegalArgumentException("MultiInputSource is not supported by GraphmlReader"));
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
                    throw sendContentError_Error("--", new IOException(e));
                }
            } catch (ParserConfigurationException e) {
                throw sendContentError_Error("--", new IOException(e));
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
            throw sendContentError_Error("--", new IllegalStateException("no schemas loaded"));

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        schemaFactory.setResourceResolver((type, namespaceURI, publicId, systemId, baseURI) -> {
            // type: http://www.w3.org/2001/XMLSchema
            // namespaceURI: http://graphml.graphdrawing.org/xmlns
            // http://www.w3.org/1999/xlink

            // publicId: null
            // baseUri: null
            // systemId: graphml-structure.xsd.xml

            log.info("Requesting resource: type=" + type + ", namespaceURI=" + namespaceURI + ", publicId=" + publicId + ", systemId=" + systemId + ", baseURI=" + baseURI);
            String content = externalSchemaMap.get(systemId);
            if (content == null) {
                log.warn("Schema resource not found for systemId: {}", systemId);
                return null;
            }
            return new SchemaInfo(content, null, null, systemId);
        });
        String graphmlSchema = externalSchemaMap.get("graphml.xsd.xml");
        if (graphmlSchema == null) {
            throw sendContentError_Error("--", new IllegalStateException("Required schema 'graphml.xsd.xml' not loaded"));
        }
        Source source = new StreamSource(new StringReader(graphmlSchema));
        factory.setSchema(schemaFactory.newSchema(source));

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

    private void loadSchemaFiles() throws IOException {
        ClassGraph cg = new ClassGraph();
        try (ResourceList list = cg.scan().getAllResources().filter(r -> r.getPath().contains("schema") && r.getPath().toLowerCase().endsWith(".xsd.xml"))) {
            for (Resource resource : list) {
                String content = resource.getContentAsString();
                int lastSlash = resource.getPath().lastIndexOf('/');
                String schemaName = resource.getPath().substring(lastSlash + 1);
                externalSchemaMap.put(schemaName, content);
            }
        }
    }

}
