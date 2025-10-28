package com.graphinout.reader.graphml.validation;

import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.xml.Sax2Log;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

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
     * Validate with GraphML Schema 1.1 including parseInfo and attribute types extensions
     *
     * @param inputSource
     */
    public static boolean isValidGraphml(InputSource inputSource) throws IOException {
        if (inputSource.isMulti()) throw new IllegalArgumentException("can only handle SingleInputSource");
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;

        try {
            String schemaAsString = IOUtils.resourceToString(xmlSchemaResource, StandardCharsets.UTF_8);
            Sax2Log errorHandler = new Sax2Log(log);

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setErrorHandler(errorHandler);
            Source schemaFile = new StreamSource(new StringReader(schemaAsString));
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.setErrorHandler(errorHandler);
            Source source = new SAXSource(new org.xml.sax.InputSource(singleInputSource.inputStream()));
            validator.validate(source);

            boolean hasErrors = errorHandler.errors() > 0;
            boolean hasFatals = errorHandler.fatals() > 0;
            boolean hasWarnings = errorHandler.warnings() > 0;

            return !hasErrors && !hasFatals && !hasWarnings;
        } catch (SAXException e) {
            log.warn("SAX Exception", e);
            return false;
        }
    }

}
