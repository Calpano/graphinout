package com.calpano.graphinout.base.xml;

import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
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
    static final String xmlSchemaResource = "/graphml-1.1.xsd.xml";
    /**
     * From http://graphml.graphdrawing.org/dtds/1.0rc/graphml.dtd
     */
    static final String dtdResource = "/graphml-1.0rc.dtd";
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
            //"<!DOCTYPE graphml SYSTEM \"http://graphml.graphdrawing.org/dtds/1.0rc/graphml.dtd\">"));
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.setErrorHandler(errorHandler);
            Source source = new SAXSource(new org.xml.sax.InputSource(singleInputSource.inputStream()));
            validator.validate(source);
            // TODO what about warnings?
            return errorHandler.errors() == 0 && errorHandler.fatals() == 0;
        } catch (SAXException e) {
            log.warn("SAX Exception",e);
            return false;
        }
    }

}
