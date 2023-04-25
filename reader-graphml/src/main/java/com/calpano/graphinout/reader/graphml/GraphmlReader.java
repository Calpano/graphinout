package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GraphmlReader implements GioReader {
    //TODO This can load from config file
    private List<String> externalSchemaList = new ArrayList<>();
    private Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("graphml", "GraphML", ".graphml", ".graphml.xml");
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

    @Override
    public boolean isValid(SingleInputSource singleInputSource) throws IOException {

        try {
            validateWellFormed(singleInputSource);
           //TODO There is no internal DTD in any of the files that have been processed so far.
           //If it exists, it can be verified with this method
           //validateInternalDTD(singleInputSource);
           //ValidateInternalXSD(singleInputSource);
           //ValidateExternalSchema(singleInputSource);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return GioReader.super.isValid(singleInputSource);
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

    void validateInternalDTD(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler(this.errorHandler));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

    void ValidateInternalXSD(SingleInputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();
        parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler(this.errorHandler));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));
    }

    void ValidateExternalSchema(SingleInputSource inputSource) throws SAXException, IOException, ParserConfigurationException {
        if (externalSchemaList.isEmpty()) return;

        Source[] listOfAvailableSchema = externalSchemaList.stream().toArray(StreamSource[]::new);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        SchemaFactory schemaFactory =
                SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        factory.setSchema(schemaFactory.newSchema(listOfAvailableSchema));

        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler(this.errorHandler));
        reader.parse(new org.xml.sax.InputSource(inputSource.inputStream()));

    }

    class SimpleErrorHandler implements ErrorHandler {


        private final Consumer<ContentError> errorConsumer;

        public SimpleErrorHandler(Consumer<ContentError> errorConsumer) {
            this.errorConsumer = errorConsumer;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            ContentError contentError = new ContentError(ContentError.ErrorLevel.Warn, e.getMessage(),
                    null);
            if (errorConsumer != null) {
                errorConsumer.accept(contentError);
            }
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            //TODO Throw exception Or log to errorConsumer?
            throw e;

        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            //TODO Throw exception Or log to errorConsumer?
            throw e;
        }
    }
}
