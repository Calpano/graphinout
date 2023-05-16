package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.annotation.Nullable;
import javax.xml.catalog.Catalog;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GraphmlReader implements GioReader {
    static class SimpleErrorHandler implements ErrorHandler {


        private final Consumer<ContentError> errorConsumer;

        public SimpleErrorHandler(Consumer<ContentError> errorConsumer) {
            this.errorConsumer = errorConsumer;
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, e.getMessage(), null);
            if (errorConsumer != null) {
                errorConsumer.accept(contentError);
            }

        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, e.getMessage(), null);
            if (errorConsumer != null) {
                errorConsumer.accept(contentError);
            }
            throw e;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            ContentError contentError = new ContentError(ContentError.ErrorLevel.Warn, e.getMessage(), null);
            if (errorConsumer != null) {
                errorConsumer.accept(contentError);
            }
        }
    }

    //TODO This can load from config file - use only GraphML 1.1
    /** lists of schema contents */
    private Map<String,String> externalSchemaMap = new HashMap<>();
    private Consumer<ContentError> errorHandler;

    public GraphmlReader() {
        try {
            loadSchemaFiles();
        } catch (IOException e) {
            ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, e.getMessage(), null);
            if (errorHandler != null) {
                errorHandler.accept(contentError);
            }
        }
    }

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("graphml", "GraphML", ".graphml", ".graphml.xml");
    }

    @Override
    public boolean isValid(SingleInputSource singleInputSource) throws IOException {

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
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setNamespaceAware(true);
            SAXParser saxParser;
            GraphmlSAXHandler saxHandler;
            try {
                saxParser = factory.newSAXParser();
                saxHandler = new GraphmlSAXHandler(writer, this.errorHandler);
            } catch (ParserConfigurationException e) {
                throw new IOException(e);
            }
            try {
                if (inputSource.isSingle())
                    saxParser.parse(((SingleInputSource) inputSource).inputStream(), saxHandler);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        } catch (Throwable t) {
            errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Failed reading '" + inputSource.name() + "'. Details: " + t.getMessage(), null));
            throw new RuntimeException("Failed reading '" + inputSource.name() + "'", t);
        }
    }

    /**
     * Two sub-options: (1) XML file contains "xsi:schemaLocation" -> take XML Schema from there; (2) we pre-downloaded
     * Graphml.XSD, use it, and ignore "xsi:schemaLocation" -- http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd
     *
     * @param inputSource
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    void validateExternalSchema(SingleInputSource inputSource) throws SAXException, IOException, ParserConfigurationException {
        if (externalSchemaMap.isEmpty()) throw new IllegalStateException("no schemas loaded");

        try {
            System.setProperty("javax.xml.catalog.files", "schema/graphinout-catalog.xml");
            CatalogFeatures features = CatalogFeatures.builder().with(CatalogFeatures.Feature.RESOLVE, "continue").build();
            URI uri = new URI("schema/graphinout-catalog.xml");
            Catalog catalog = CatalogManager.catalog(features);
            CatalogResolver cr = CatalogManager.catalogResolver( catalog);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            //schemaFactory.setResourceResolver(cr);
            schemaFactory.setResourceResolver(new LSResourceResolver() {
                @Override
                public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                    log.info("Requesting resource: type=" + type + ", namespaceURI=" + namespaceURI + ", publicId=" + publicId + ", systemId=" + systemId + ", baseURI=" + baseURI);
                    return cr.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
                }
            });
            Source source = new StreamSource(new StringReader(externalSchemaMap.get("graphml.xsd.xml")));
            factory.setSchema(schemaFactory.newSchema(source));

            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(new SimpleErrorHandler(this.errorHandler));
            reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
        } catch ( URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    void validateInternalDTD(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler(this.errorHandler));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

    void validateInternalXSD(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();
        parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler(this.errorHandler));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

    void validateWellFormed(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler(this.errorHandler));
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

    public @Nullable String getSchema(String localSchemaResourceName)  {
        return externalSchemaMap.get(localSchemaResourceName);
    }
}
