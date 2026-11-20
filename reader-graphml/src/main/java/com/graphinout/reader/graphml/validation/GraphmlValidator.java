package com.graphinout.reader.graphml.validation;

import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.xml.factory.XmlFactory;
import com.graphinout.base.xml.sax.SaxErrors2Log;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlValidator {

    /**
     * includes parseInfo and attribute types extension; from http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd
     */
    static final String xmlSchemaResource = "/schema/graphml/graphml-1.1.xsd.xml";
    /**
     * From http://graphml.graphdrawing.org/dtds/1.0rc/graphml.dtd
     */
    static final String dtdResource = "/schema/graphml/graphml-1.0rc.dtd";
    private static final Logger log = getLogger(GraphmlValidator.class);

    /**
     * The compiled GraphML 1.1 schema. {@link Schema} is immutable and thread-safe, the schema never varies per input
     * document, and compiling it is expensive, so it is built once per JVM and reused. Only the cheap (and not
     * thread-safe) {@link Validator} is created per call via {@link Schema#newValidator()}.
     * <p>
     * Built lazily and thread-safely via the initialization-on-demand holder idiom.
     */
    private static final class SchemaHolder {
        static final SAXException ERROR;
        static final Schema SCHEMA;

        static {
            Schema schema = null;
            SAXException error = null;
            try {
                String schemaAsString = IOUtils.resourceToString(xmlSchemaResource, StandardCharsets.UTF_8);
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setErrorHandler(new SaxErrors2Log(log));
                // resolve the GraphML sub-schemas (referenced by absolute graphdrawing.org URLs) to bundled local
                // resources, so validation works offline (e.g. in CI) instead of fetching them over the network
                // schemas come only from bundled resources (via the resolver below), never from files or the network
                factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setResourceResolver(new BundledSchemaResolver());
                Source schemaFile = new StreamSource(new StringReader(schemaAsString));
                schema = factory.newSchema(schemaFile);
            } catch (IOException e) {
                error = new SAXException("Failed to load GraphML schema resource", e);
            } catch (SAXException e) {
                error = e;
            }
            SCHEMA = schema;
            ERROR = error;
        }
    }

    /**
     * Validate with GraphML Schema 1.1 including parseInfo and attribute types extensions
     *
     * @param inputSource
     */
    public static boolean isValidGraphml(InputSource inputSource) throws IOException {
        return validate(inputSource).isEmpty();
    }

    /**
     * Validate with GraphML Schema 1.1 including parseInfo and attribute types extensions.
     * <p>
     * Meant for checking GraphML we write ourselves. Real-world input is rarely schema-valid (yEd extensions, ad-hoc
     * {@code <data>} content), so the reader does not validate input against the XSD (see #115).
     *
     * @return one message per schema finding (warnings, errors and fatal errors), with line and column; empty if valid
     */
    public static List<String> validate(InputSource inputSource) throws IOException {
        if (inputSource.isMulti()) throw new IllegalArgumentException("can only handle SingleInputSource");
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;

        CollectingSaxErrorHandler errorHandler = new CollectingSaxErrorHandler();
        try {
            if (SchemaHolder.ERROR != null) {
                throw SchemaHolder.ERROR;
            }
            Validator validator = SchemaHolder.SCHEMA.newValidator();
            validator.setErrorHandler(errorHandler);
            // the input is untrusted: parse it with an XXE-hardened reader, and let the validator fetch nothing either
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            XMLReader xmlReader = XmlFactory.hardenedSaxParserFactory().newSAXParser().getXMLReader();
            Source source = new SAXSource(xmlReader, new org.xml.sax.InputSource(singleInputSource.inputStream()));
            validator.validate(source);
        } catch (SAXParseException e) {
            // a fatal error is reported to the handler before it is thrown; anything else (e.g. a broken bundled
            // schema) is not, and must not be lost
            if (errorHandler.messages().isEmpty()) {
                log.warn("SAX Exception", e);
                errorHandler.add("Fatal", e.getMessage());
            }
        } catch (SAXException e) {
            log.warn("SAX Exception", e);
            errorHandler.add("Fatal", e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Cannot create a hardened XML parser", e);
        }
        return errorHandler.messages();
    }

    /**
     * Collects SAX validation findings, logging them only at DEBUG. A schema-validation failure on the input is a
     * content problem, not infrastructure noise, and this validator reports it to its caller.
     */
    private static final class CollectingSaxErrorHandler implements ErrorHandler {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void error(SAXParseException exception) {
            log.debug("SAX error", exception);
            add("Error", exception);
        }

        @Override
        public void fatalError(SAXParseException exception) {
            log.debug("SAX fatal error", exception);
            add("Fatal", exception);
        }

        @Override
        public void warning(SAXParseException exception) {
            log.debug("SAX warning", exception);
            add("Warning", exception);
        }

        private void add(String level, SAXParseException e) {
            add(level, e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage());
        }

        void add(String level, String message) {
            messages.add(level + " " + message);
        }

        List<String> messages() {
            return List.copyOf(messages);
        }
    }

    /**
     * Resolves the GraphML schema's external references (e.g. the absolute URL
     * {@code http://graphml.graphdrawing.org/xmlns/1.1/graphml-structure.xsd} and the relative {@code xlink.xsd.xml})
     * to the schema files bundled under {@code /schema/}, so that schema building needs no network access.
     */
    private static final class BundledSchemaResolver implements LSResourceResolver {

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            if (systemId == null) {
                return null;
            }
            String fileName = systemId.substring(systemId.lastIndexOf('/') + 1);
            String resourcePath = switch (fileName) {
                case "graphml-structure.xsd", "graphml-structure.xsd.xml" -> "/schema/graphml-structure.xsd.xml";
                case "graphml-attributes.xsd", "graphml-attributes.xsd.xml" -> "/schema/graphml-attributes.xsd.xml";
                case "graphml-parseinfo.xsd", "graphml-parseinfo.xsd.xml" -> "/schema/graphml-parseinfo.xsd.xml";
                case "xlink.xsd", "xlink.xsd.xml" -> "/schema/xlink.xsd.xml";
                default -> null;
            };
            if (resourcePath == null) {
                log.warn("No bundled GraphML schema for systemId '{}'", systemId);
                return null;
            }
            InputStream in = GraphmlValidator.class.getResourceAsStream(resourcePath);
            if (in == null) {
                log.warn("Bundled GraphML schema resource not found: {}", resourcePath);
                return null;
            }
            return new ResourceLSInput(in, publicId, systemId, baseURI);
        }
    }

    /** Minimal {@link LSInput} backed by a byte stream from a bundled resource. */
    private static final class ResourceLSInput implements LSInput {

        private InputStream byteStream;
        private String publicId;
        private String systemId;
        private String baseURI;

        ResourceLSInput(InputStream byteStream, String publicId, String systemId, String baseURI) {
            this.byteStream = byteStream;
            this.publicId = publicId;
            this.systemId = systemId;
            this.baseURI = baseURI;
        }

        @Override
        public InputStream getByteStream() {
            return byteStream;
        }

        @Override
        public void setByteStream(InputStream byteStream) {
            this.byteStream = byteStream;
        }

        @Override
        public Reader getCharacterStream() {
            return null;
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
        }

        @Override
        public String getStringData() {
            return null;
        }

        @Override
        public void setStringData(String stringData) {
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public void setPublicId(String publicId) {
            this.publicId = publicId;
        }

        @Override
        public String getBaseURI() {
            return baseURI;
        }

        @Override
        public void setBaseURI(String baseURI) {
            this.baseURI = baseURI;
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public void setEncoding(String encoding) {
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
        }
    }

}
